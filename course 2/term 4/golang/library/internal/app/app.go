package app

import (
	"context"
	"encoding/json"
	"fmt"
	"math/rand"
	"net"
	"net/http"
	"os"
	"os/signal"
	"runtime"
	"strings"
	"syscall"
	"time"

	"github.com/grafana/pyroscope-go"
	grpcruntime "github.com/grpc-ecosystem/grpc-gateway/v2/runtime"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/project/library/config"
	"github.com/project/library/db"
	generated "github.com/project/library/generated/api/library"
	"github.com/project/library/internal/controller"
	"github.com/project/library/internal/entity"
	"github.com/project/library/internal/usecase/library"
	"github.com/project/library/internal/usecase/outbox"
	"github.com/project/library/internal/usecase/repository"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"go.opentelemetry.io/contrib/instrumentation/google.golang.org/grpc/otelgrpc"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/jaeger"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.21.0"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/reflection"
)

func runTestServer() {
	mux := http.NewServeMux()
	rand.NewSource(time.Now().UnixNano())

	mux.HandleFunc("/book", func(w http.ResponseWriter, r *http.Request) {
		if rand.Intn(5) == 0 {
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte("500 - Internal Server Error (simulated)"))
			return
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("Book processed"))
	})

	mux.HandleFunc("/author", func(w http.ResponseWriter, r *http.Request) {
		if rand.Intn(100) < 10 {
			errors := []int{
				http.StatusInternalServerError,
				http.StatusBadGateway,
				http.StatusServiceUnavailable,
			}
			status := errors[rand.Intn(len(errors))]

			w.WriteHeader(status)
			w.Write([]byte(fmt.Sprintf("%d - Simulated Error", status)))
			return
		}

		w.WriteHeader(http.StatusOK)
		w.Write([]byte("Author processed"))
	})

	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if rand.Intn(10) == 0 {
			w.WriteHeader(http.StatusNotFound)
			w.Write([]byte("404 - Not Found"))
			return
		}
		w.WriteHeader(http.StatusOK)
	})

	srv := &http.Server{
		Addr:    ":9999",
		Handler: mux,
	}

	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			fmt.Printf("test server error: %v\n", err)
		}
	}()
}

func Run(logger *zap.Logger, cfg *config.Config) {
	ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	shutdown := initTracer(logger, cfg.Observability.JaegerURL)

	defer func() {
		err := shutdown(ctx)

		if err != nil {
			logger.Error("cannot shutdown jaeger collector", zap.Error(err))
		}
	}()

	runTestServer()
	go runMetricsServer(cfg.Observability.MetricsPort)
	runPyroscope(logger)

	dbPool, err := pgxpool.New(ctx, cfg.PG.URL)

	if err != nil {
		logger.Error("can not create pgxpool", zap.Error(err))
		//nolint:gocritic // canceled
		os.Exit(1)
	}

	defer dbPool.Close()

	db.SetupPostgres(dbPool, logger)

	repo := repository.NewPostgresRepository(dbPool)
	outboxRepository := repository.NewOutbox(dbPool)

	transactor := repository.NewTransactor(dbPool)
	runOutbox(ctx, cfg, logger, outboxRepository, transactor)

	useCases := library.New(logger, repo, repo, outboxRepository, transactor)

	ctrl := controller.New(logger, useCases, useCases)

	go runRest(ctx, cfg, logger)
	go runGrpc(cfg, logger, ctrl)

	<-ctx.Done()
	time.Sleep(time.Second)
}

func initTracer(l *zap.Logger, url string) func(context.Context) error {
	exp, err := jaeger.New(jaeger.WithCollectorEndpoint(jaeger.WithEndpoint(url)))

	if err != nil {
		l.Fatal("cannot create jaeger collector", zap.Error(err))
	}

	tp := trace.NewTracerProvider(
		trace.WithBatcher(exp),
		trace.WithResource(resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceName("library-service"),
		)),
	)

	otel.SetTracerProvider(tp)

	return tp.Shutdown
}

func runRest(ctx context.Context, cfg *config.Config, logger *zap.Logger) {
	mux := grpcruntime.NewServeMux()
	opts := []grpc.DialOption{grpc.WithTransportCredentials(insecure.NewCredentials())}

	address := "localhost:" + cfg.GRPC.Port
	err := generated.RegisterLibraryHandlerFromEndpoint(ctx, mux, address, opts)
	if err != nil {
		logger.Error("can not register grpc gateway", zap.Error(err))
		os.Exit(1)
	}

	gatewayPort := ":" + cfg.GRPC.GatewayPort
	logger.Info("gateway listening at port", zap.String("port", gatewayPort))

	if err = http.ListenAndServe(gatewayPort, mux); err != nil {
		logger.Error("gateway listen error", zap.Error(err))
	}
}

func runGrpc(cfg *config.Config, logger *zap.Logger, libraryService generated.LibraryServer) {
	port := ":" + cfg.GRPC.Port
	lis, err := net.Listen("tcp", port)

	if err != nil {
		logger.Error("can not open tcp socket", zap.Error(err))
		os.Exit(1)
	}

	srvHandler := otelgrpc.NewServerHandler(
		otelgrpc.WithTracerProvider(otel.GetTracerProvider()),
	)
	s := grpc.NewServer(
		grpc.StatsHandler(srvHandler),
	)

	reflection.Register(s)

	generated.RegisterLibraryServer(s, libraryService)

	logger.Info("grpc server listening at port", zap.String("port", port))

	if err = s.Serve(lis); err != nil {
		logger.Error("grpc server listen error", zap.Error(err))
	}
}

func runOutbox(
	ctx context.Context,
	cfg *config.Config,
	logger *zap.Logger,
	outboxRepository repository.OutboxRepository,
	transactor repository.Transactor,
) {
	//nolint:mnd // timeouts
	dialer := &net.Dialer{
		Timeout:   30 * time.Second,
		KeepAlive: 180 * time.Second,
	}

	//nolint:mnd // timeouts
	transport := &http.Transport{
		DialContext:           dialer.DialContext,
		ForceAttemptHTTP2:     true,
		MaxIdleConns:          100,
		MaxConnsPerHost:       100,
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   15 * time.Second,
		ExpectContinueTimeout: 2 * time.Second,
		MaxIdleConnsPerHost:   runtime.GOMAXPROCS(0) + 1,
	}

	client := new(http.Client)
	client.Transport = transport

	globalHandler := globalOutboxHandler(client, cfg.Outbox.BookSendURL, cfg.Outbox.AuthorSendURL)
	outboxService := outbox.New(logger, outboxRepository, globalHandler, cfg, transactor)

	outboxService.Start(
		ctx,
		cfg.Outbox.Workers,
		cfg.Outbox.BatchSize,
		cfg.Outbox.WaitTimeMS,
		cfg.Outbox.InProgressTTLMS,
	)
}

func runPyroscope(l *zap.Logger) {
	runtime.SetMutexProfileFraction(1)
	runtime.SetBlockProfileRate(1)
	_, err := pyroscope.Start(pyroscope.Config{
		ApplicationName: "leak.app",
		ServerAddress:   "http://pyroscope:4040",
		Logger:          pyroscope.StandardLogger,
		ProfileTypes: []pyroscope.ProfileType{
			pyroscope.ProfileCPU,
			pyroscope.ProfileAllocObjects,
			pyroscope.ProfileAllocSpace,
			pyroscope.ProfileInuseObjects,
			pyroscope.ProfileInuseSpace,

			pyroscope.ProfileGoroutines,
			pyroscope.ProfileMutexCount,
			pyroscope.ProfileMutexDuration,
			pyroscope.ProfileBlockCount,
			pyroscope.ProfileBlockDuration,
		},
	})

	if err != nil {
		l.Fatal("can not set up pyroscope", zap.Error(err))
	}
}

func runMetricsServer(port string) {
	http.Handle("/metrics", promhttp.Handler())
	http.ListenAndServe(":"+port, nil)
}

func globalOutboxHandler(
	client *http.Client,
	bookURL string,
	authorURL string,
) outbox.GlobalHandler {
	return func(kind repository.OutboxKind) (outbox.KindHandler, error) {
		switch kind {
		case repository.OutboxKindBook:
			return bookOutboxHandler(client, bookURL), nil
		case repository.OutboxKindAuthor:
			return authorOutboxHandler(client, authorURL), nil
		default:
			return nil, fmt.Errorf("unsupported outbox kind: %d", kind)
		}
	}
}

func bookOutboxHandler(client *http.Client, url string) outbox.KindHandler {
	return func(ctx context.Context, data []byte) error {
		book := entity.Book{}
		err := json.Unmarshal(data, &book)

		if err != nil {
			return fmt.Errorf("can not deserialize data in book outbox handler: %w", err)
		}

		body := strings.NewReader(book.ID)

		req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, body)
		if err != nil {
			return fmt.Errorf("cannot create HTTP request: %w", err)
		}

		resp, err := client.Do(req)
		if err != nil {
			return fmt.Errorf("error sending book to %s: %w", url, err)
		}
		defer resp.Body.Close()

		if resp.StatusCode >= http.StatusInternalServerError {
			return fmt.Errorf("server error: status %d", resp.StatusCode)
		}

		if resp.StatusCode < http.StatusOK || resp.StatusCode >= http.StatusMultipleChoices {
			return fmt.Errorf("unexpected status: %d", resp.StatusCode)
		}

		return nil
	}
}

func authorOutboxHandler(client *http.Client, url string) outbox.KindHandler {
	return func(ctx context.Context, data []byte) error {
		author := entity.Author{}
		err := json.Unmarshal(data, &author)

		if err != nil {
			return fmt.Errorf("can not deserialize data in author outbox handler: %w", err)
		}

		body := strings.NewReader(author.ID)

		req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, body)
		if err != nil {
			return fmt.Errorf("cannot create HTTP request: %w", err)
		}

		resp, err := client.Do(req)
		if err != nil {
			return fmt.Errorf("error sending author to %s: %w", url, err)
		}
		defer resp.Body.Close()

		if resp.StatusCode >= http.StatusInternalServerError {
			return fmt.Errorf("server error: status %d", resp.StatusCode)
		}

		if resp.StatusCode < http.StatusOK || resp.StatusCode >= http.StatusMultipleChoices {
			return fmt.Errorf("unexpected status: %d", resp.StatusCode)
		}

		return nil
	}
}

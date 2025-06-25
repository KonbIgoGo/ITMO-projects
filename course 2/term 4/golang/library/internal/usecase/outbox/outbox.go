package outbox

import (
	"context"
	"sync"
	"time"

	"github.com/project/library/config"
	"github.com/project/library/internal/usecase/repository"
	"github.com/prometheus/client_golang/prometheus"
	"go.uber.org/zap"
)

var (
	outboxSuccessTotal = prometheus.NewCounter(prometheus.CounterOpts{
		Name: "outbox_success_total",
		Help: "Total number of successfully processed outbox messages",
	})

	outboxFailTotal = prometheus.NewCounter(prometheus.CounterOpts{
		Name: "outbox_fail_total",
		Help: "Total number of failed outbox messages processing attempts",
	})
)

func init() {
	prometheus.MustRegister(outboxSuccessTotal, outboxFailTotal)
}

type GlobalHandler = func(kind repository.OutboxKind) (KindHandler, error)
type KindHandler = func(ctx context.Context, data []byte) error

type Outbox interface {
	Start(ctx context.Context, workers int, batchSize int, waitTime time.Duration, inProgressTTL time.Duration)
}

var _ Outbox = (*outboxImpl)(nil)

type outboxImpl struct {
	logger           *zap.Logger
	outboxRepository repository.OutboxRepository
	globalHandler    GlobalHandler
	cfg              *config.Config
	transactor       repository.Transactor
}

func New(
	logger *zap.Logger,
	outboxRepository repository.OutboxRepository,
	globalHandler GlobalHandler,
	cfg *config.Config,
	transactor repository.Transactor,
) *outboxImpl {
	return &outboxImpl{
		logger:           logger,
		outboxRepository: outboxRepository,
		globalHandler:    globalHandler,
		cfg:              cfg,
		transactor:       transactor,
	}
}

func (o *outboxImpl) Start(
	ctx context.Context,
	workers int,
	batchSize int,
	waitTime time.Duration,
	inProgressTTL time.Duration,
) {
	wg := new(sync.WaitGroup)

	for workerID := 1; workerID <= workers; workerID++ {
		wg.Add(1)
		go o.worker(ctx, wg, batchSize, waitTime, inProgressTTL)
	}
}

func (o *outboxImpl) worker(
	ctx context.Context,
	wg *sync.WaitGroup,
	batchSize int,
	waitTIme time.Duration,
	inProgressTTL time.Duration,
) {
	defer wg.Done()

	for {
		time.Sleep(waitTIme)

		if !o.cfg.Outbox.Enabled {
			continue
		}

		err := o.transactor.WithTx(ctx, func(ctx context.Context) error {
			messages, err := o.outboxRepository.GetMessages(ctx, batchSize, inProgressTTL)
			if err != nil {
				o.logger.Warn("can not fetch messages from outbox", zap.Error(err))
				return err
			}

			o.logger.Info("messages fetched", zap.Int("size", len(messages)))
			successKeys := make([]string, 0, len(messages))

			for i := 0; i < len(messages); i++ {
				message := messages[i]
				key := message.IdempotencyKey

				var kindHandler KindHandler
				kindHandler, err = o.globalHandler(message.Kind)
				if err != nil {
					o.logger.Warn("unexpected kind", zap.Error(err))
					continue
				}

				err = kindHandler(ctx, message.RawData)
				if err != nil {
					o.logger.Warn("kind error", zap.Error(err))
					continue
				}

				successKeys = append(successKeys, key)
			}

			outboxFailTotal.Add(float64(len(messages) - len(successKeys)))
			outboxSuccessTotal.Add(float64(len(successKeys)))

			err = o.outboxRepository.MarkAsProcessed(ctx, successKeys)
			if err != nil {
				o.logger.Warn("mark as processed outbox error", zap.Error(err))
				return err
			}

			return nil
		})

		if err != nil {
			o.logger.Warn("worker stage error", zap.Error(err))
		}
	}
}

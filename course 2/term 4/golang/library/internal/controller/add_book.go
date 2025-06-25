package controller

import (
	"context"
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"go.opentelemetry.io/otel/trace"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var AddBookDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_add_book_duration",
	Help:    "Duration of AddBook",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(AddBookDuration)
}

func (i *implementation) AddBook(ctx context.Context, req *library.AddBookRequest) (*library.AddBookResponse, error) {
	start := time.Now()

	defer func() {
		AddBookDuration.Observe(float64(time.Since(start).Seconds()))
	}()

	span := trace.SpanFromContext(ctx)
	defer span.End()

	if err := req.ValidateAll(); err != nil {
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	if err := i.validateAuthors(ctx, req.GetAuthorIds()); err != nil {
		return nil, i.convertErr(err)
	}

	book, err := i.booksUseCase.RegisterBook(ctx, req.GetName(), req.GetAuthorIds())
	if err != nil {
		return nil, i.convertErr(err)
	}

	return &library.AddBookResponse{
		Book: &library.Book{
			Id:        book.ID,
			Name:      book.Name,
			AuthorId:  book.AuthorIDs,
			CreatedAt: timestamppb.New(book.CreatedAt),
			UpdatedAt: timestamppb.New(book.UpdatedAt),
		},
	}, nil
}

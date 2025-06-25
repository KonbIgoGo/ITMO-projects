package controller

import (
	"context"
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var UpdateBookDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_update_book_duration",
	Help:    "Duration of UpdateBook",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(UpdateBookDuration)
}

func (i *implementation) UpdateBook(ctx context.Context, req *library.UpdateBookRequest) (*library.UpdateBookResponse, error) {
	start := time.Now()

	defer func() {
		UpdateBookDuration.Observe(float64(time.Since(start).Seconds()))
	}()
	if err := req.ValidateAll(); err != nil {
		i.logger.Error("Validation error", zap.Error(err))
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	if err := i.validateAuthors(ctx, req.GetAuthorIds()); err != nil {
		i.logger.Error("Validation authors of book error", zap.Error(err))
		return nil, i.convertErr(err)
	}

	var err = i.booksUseCase.UpdateBook(ctx, req.GetId(), req.GetName(), req.GetAuthorIds())
	if err != nil {
		i.logger.Error("Update book error", zap.Error(err))
		return nil, i.convertErr(err)
	}

	return &library.UpdateBookResponse{}, nil
}

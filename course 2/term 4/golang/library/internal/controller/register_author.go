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

var RegisterAuthorDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_register_author_duration",
	Help:    "Duration of RegisterAuthor",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(RegisterAuthorDuration)
}

func (i *implementation) RegisterAuthor(ctx context.Context, req *library.RegisterAuthorRequest) (*library.RegisterAuthorResponse, error) {
	start := time.Now()

	defer func() {
		RegisterAuthorDuration.Observe(float64(time.Since(start).Seconds()))
	}()
	if err := req.ValidateAll(); err != nil {
		i.logger.Error("Validation error", zap.Error(err))
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	author, err := i.authorUseCase.RegisterAuthor(ctx, req.GetName())
	if err != nil {
		i.logger.Error("Register author error", zap.Error(err))
		return nil, i.convertErr(err)
	}

	var res = &library.RegisterAuthorResponse{
		Id: author.ID,
	}

	return res, nil
}

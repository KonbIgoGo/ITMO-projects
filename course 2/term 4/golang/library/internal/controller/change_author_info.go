package controller

import (
	"context"
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var ChangeAuthorInfoDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_change_author_info_duration",
	Help:    "Duration of ChangeAuthorInfo",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(ChangeAuthorInfoDuration)
}

func (i *implementation) ChangeAuthorInfo(ctx context.Context, req *library.ChangeAuthorInfoRequest) (*library.ChangeAuthorInfoResponse, error) {
	start := time.Now()

	defer func() {
		ChangeAuthorInfoDuration.Observe(float64(time.Since(start).Seconds()))
	}()
	if err := req.ValidateAll(); err != nil {
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	if err := i.authorUseCase.ChangeAuthorInfo(ctx, req.GetId(), req.GetName()); err != nil {
		return nil, i.convertErr(err)
	}

	return &library.ChangeAuthorInfoResponse{}, nil
}

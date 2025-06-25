package controller

import (
	"context"
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var GetAuthorInfoDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_get_author_info_duration",
	Help:    "Duration of GetAuthorInfo",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(GetAuthorInfoDuration)
}

func (i *implementation) GetAuthorInfo(ctx context.Context, req *library.GetAuthorInfoRequest) (*library.GetAuthorInfoResponse, error) {
	start := time.Now()

	defer func() {
		GetAuthorInfoDuration.Observe(float64(time.Since(start).Seconds()))
	}()
	if err := req.ValidateAll(); err != nil {
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	info, err := i.authorUseCase.GetAuthor(ctx, req.GetId())
	if err != nil {
		return nil, i.convertErr(err)
	}

	return &library.GetAuthorInfoResponse{
		Id:   info.ID,
		Name: info.Name,
	}, nil
}

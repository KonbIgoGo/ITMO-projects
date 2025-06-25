package controller

import (
	"context"
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var GetBookInfoDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_get_book_info_duration",
	Help:    "Duration of GetBookInfo",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(GetBookInfoDuration)
}

func (i *implementation) GetBookInfo(ctx context.Context, req *library.GetBookInfoRequest) (*library.GetBookInfoResponse, error) {
	start := time.Now()

	defer func() {
		GetBookInfoDuration.Observe(float64(time.Since(start).Seconds()))
	}()

	if err := req.ValidateAll(); err != nil {
		return nil, status.Error(codes.InvalidArgument, err.Error())
	}

	info, err := i.booksUseCase.GetBook(ctx, req.GetId())
	if err != nil {
		return nil, i.convertErr(err)
	}

	return &library.GetBookInfoResponse{
		Book: &library.Book{
			Id:        info.ID,
			Name:      info.Name,
			AuthorId:  info.AuthorIDs,
			CreatedAt: timestamppb.New(info.CreatedAt),
			UpdatedAt: timestamppb.New(info.UpdatedAt),
		},
	}, nil
}

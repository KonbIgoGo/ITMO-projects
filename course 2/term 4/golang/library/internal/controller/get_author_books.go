package controller

import (
	"time"

	"github.com/project/library/generated/api/library"
	"github.com/prometheus/client_golang/prometheus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var GetAuthorBooksDuration = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "library_get_author_books_duration",
	Help:    "Duration of GetAuthorBooks",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(GetAuthorBooksDuration)
}

func (i *implementation) GetAuthorBooks(req *library.GetAuthorBooksRequest, str library.Library_GetAuthorBooksServer) error {
	start := time.Now()

	defer func() {
		GetAuthorBooksDuration.Observe(float64(time.Since(start).Seconds()))
	}()

	if err := req.ValidateAll(); err != nil {
		return status.Error(codes.InvalidArgument, err.Error())
	}

	author, err := i.authorUseCase.GetAuthor(str.Context(), req.GetAuthorId())
	if err != nil {
		return i.convertErr(err)
	}

	books, err := i.authorUseCase.CollectAuthorBooks(str.Context(), author)
	if err != nil {
		return i.convertErr(err)
	}

	for _, b := range books {
		err := str.Send(&library.Book{
			Id:        b.ID,
			Name:      b.Name,
			AuthorId:  b.AuthorIDs,
			CreatedAt: timestamppb.New(b.CreatedAt),
			UpdatedAt: timestamppb.New(b.UpdatedAt),
		})
		if err != nil {
			return i.convertErr(err)
		}
	}

	return nil
}

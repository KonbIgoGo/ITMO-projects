package controller

import (
	"testing"

	"github.com/project/library/generated/api/library"
	"github.com/project/library/internal/mocks"
	"github.com/stretchr/testify/require"
	"go.uber.org/mock/gomock"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func TestGetAuthorBooks(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	lib := mocks.NewMockBookUseCase(ctrl)
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(&zap.Logger{}, lib, libAuthors)
	type testCase struct {
		authorID    string
		errExpected error
	}

	t.Run("test author get books validation", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				authorID:    "incorrectID",
				errExpected: status.Error(codes.InvalidArgument, "invalid uuid format"),
			},
		}

		for _, tc := range tcs {
			err := impl.GetAuthorBooks(&library.GetAuthorBooksRequest{AuthorId: tc.authorID}, nil)
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, s.Code(), status.Code(tc.errExpected))
			} else {
				require.NoError(t, err)
			}
		}
	})
}

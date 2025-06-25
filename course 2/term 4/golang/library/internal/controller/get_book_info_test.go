package controller

import (
	"context"
	"testing"

	"github.com/google/uuid"
	"github.com/project/library/generated/api/library"
	"github.com/project/library/internal/entity"
	"github.com/project/library/internal/mocks"
	"github.com/stretchr/testify/require"
	"go.uber.org/mock/gomock"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func TestGetBookInfo(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libBooks := mocks.NewMockBookUseCase(ctrl)

	impl := New(nil, libBooks, nil)

	type testCase struct {
		bookID string

		errExpected error
	}

	t.Run("test get book info validation", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				bookID:      "incorrectID",
				errExpected: status.Error(codes.InvalidArgument, "invalid uuid format"),
			},
			{
				bookID:      uuid.NewString(),
				errExpected: nil,
			},
		}

		for _, tc := range tcs {
			if tc.errExpected == nil {
				libBooks.EXPECT().GetBook(ctx, tc.bookID).Return(entity.Book{ID: tc.bookID}, nil)
			}
			resp, err := impl.GetBookInfo(ctx, &library.GetBookInfoRequest{Id: tc.bookID})

			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, s.Code(), status.Code(tc.errExpected))
				require.Empty(t, resp)
			} else {
				require.NoError(t, err)
				require.Equal(t, resp.GetBook().GetId(), tc.bookID)
			}
		}
	})

	t.Run("test get book not found error", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				bookID:      uuid.NewString(),
				errExpected: nil,
			},
			{
				bookID:      uuid.NewString(),
				errExpected: entity.ErrAuthorNotFound,
			},
		}

		for _, tc := range tcs {
			if tc.errExpected != nil {
				libBooks.EXPECT().GetBook(ctx, tc.bookID).Return(entity.Book{}, tc.errExpected)
			} else {
				libBooks.EXPECT().GetBook(ctx, tc.bookID).Return(entity.Book{ID: tc.bookID}, nil)
			}

			resp, err := impl.GetBookInfo(ctx, &library.GetBookInfoRequest{Id: tc.bookID})
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, codes.NotFound, s.Code())
				require.Empty(t, resp)
			} else {
				require.NoError(t, err)
				require.Equal(t, resp.GetBook().GetId(), tc.bookID)
			}
		}
	})
}

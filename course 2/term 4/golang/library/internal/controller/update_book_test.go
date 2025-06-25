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

func TestUpdateBook(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libBooks := mocks.NewMockBookUseCase(ctrl)
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(nil, libBooks, libAuthors)

	type testCase struct {
		bookID      string
		authorsIDs  []string
		errExpected error
	}

	t.Run("test book validation", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				bookID:      uuid.NewString(),
				authorsIDs:  []string{},
				errExpected: nil,
			},
			{
				bookID:      "incorrectID",
				errExpected: status.Error(codes.InvalidArgument, "invalid uuid format"),
			},
		}

		for _, tc := range tcs {
			if tc.errExpected == nil {
				libBooks.EXPECT().UpdateBook(ctx, tc.bookID, "name", tc.authorsIDs).Return(nil)
			}

			resp, err := impl.UpdateBook(ctx, &library.UpdateBookRequest{Id: tc.bookID, Name: "name", AuthorIds: tc.authorsIDs})

			require.Empty(t, resp)
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, s.Code(), status.Code(tc.errExpected))
			} else {
				require.NoError(t, err)
			}
		}
	})

	t.Run("test book not found", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				bookID:      uuid.NewString(),
				authorsIDs:  []string{},
				errExpected: nil,
			},
			{
				bookID:      uuid.NewString(),
				authorsIDs:  []string{},
				errExpected: entity.ErrBookNotFound,
			},
		}

		for _, tc := range tcs {
			libBooks.EXPECT().UpdateBook(ctx, tc.bookID, "name", tc.authorsIDs).Return(tc.errExpected)

			resp, err := impl.UpdateBook(ctx, &library.UpdateBookRequest{Id: tc.bookID, Name: "name", AuthorIds: tc.authorsIDs})

			require.Empty(t, resp)
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, codes.NotFound, s.Code())
			} else {
				require.NoError(t, err)
			}
		}
	})

	t.Run("test author not found", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				bookID:      uuid.NewString(),
				authorsIDs:  []string{uuid.NewString(), uuid.NewString()},
				errExpected: nil,
			},
			{
				bookID:      uuid.NewString(),
				authorsIDs:  []string{"Not exist ID"},
				errExpected: entity.ErrAuthorNotFound,
			},
		}

		for _, tc := range tcs {
			if tc.errExpected != nil {
				libAuthors.EXPECT().GetAuthor(ctx, tc.authorsIDs[0]).Return(entity.Author{}, tc.errExpected)
			} else {
				for _, id := range tc.authorsIDs {
					libAuthors.EXPECT().GetAuthor(ctx, id).Return(entity.Author{ID: id}, nil)
				}
				libBooks.EXPECT().UpdateBook(ctx, tc.bookID, "name", tc.authorsIDs)
			}

			resp, err := impl.UpdateBook(ctx, &library.UpdateBookRequest{Id: tc.bookID, Name: "name", AuthorIds: tc.authorsIDs})

			require.Empty(t, resp)
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, codes.NotFound, s.Code())
			} else {
				require.NoError(t, err)
			}
		}
	})
}

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

func TestGetAuthorInfo(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(nil, nil, libAuthors)

	type testCase struct {
		authorID    string
		errExpected error
	}

	t.Run("test get author info validation", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				authorID:    "incorrectID",
				errExpected: status.Error(codes.InvalidArgument, "invalid uuid format"),
			},
			{
				authorID:    uuid.NewString(),
				errExpected: nil,
			},
		}

		for _, tc := range tcs {
			if tc.errExpected == nil {
				libAuthors.EXPECT().GetAuthor(ctx, tc.authorID).Return(entity.Author{ID: tc.authorID}, nil)
			}

			resp, err := impl.GetAuthorInfo(ctx, &library.GetAuthorInfoRequest{Id: tc.authorID})
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, s.Code(), status.Code(tc.errExpected))
				require.Empty(t, resp)
			} else {
				require.NoError(t, err)
				require.Equal(t, resp.GetId(), tc.authorID)
			}
		}
	})

	t.Run("test get author not found error", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				authorID:    uuid.NewString(),
				errExpected: nil,
			},
			{
				authorID:    uuid.NewString(),
				errExpected: entity.ErrAuthorNotFound,
			},
		}

		for _, tc := range tcs {
			if tc.errExpected != nil {
				libAuthors.EXPECT().GetAuthor(ctx, tc.authorID).Return(entity.Author{}, tc.errExpected)
			} else {
				libAuthors.EXPECT().GetAuthor(ctx, tc.authorID).Return(entity.Author{ID: tc.authorID}, nil)
			}

			resp, err := impl.GetAuthorInfo(ctx, &library.GetAuthorInfoRequest{Id: tc.authorID})
			if tc.errExpected != nil {
				s, ok := status.FromError(err)
				require.True(t, ok)
				require.Equal(t, codes.NotFound, s.Code())
				require.Empty(t, resp)
			} else {
				require.NoError(t, err)
				require.Equal(t, resp.GetId(), tc.authorID)
			}
		}
	})
}

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

func TestChangeAuthorInfo(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(nil, nil, libAuthors)

	type testCase struct {
		authorID    string
		name        string
		errExpected error
	}

	t.Run("test validation change author info", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				authorID:    uuid.NewString(),
				name:        "new name",
				errExpected: nil,
			},
			{
				authorID:    "incorrectID",
				name:        "incorrect author",
				errExpected: status.Error(codes.InvalidArgument, "invalid uuid format"),
			},
		}

		for _, tc := range tcs {
			if tc.errExpected == nil {
				libAuthors.EXPECT().ChangeAuthorInfo(ctx, tc.authorID, tc.name).Return(nil)
			}

			resp, err := impl.ChangeAuthorInfo(ctx, &library.ChangeAuthorInfoRequest{Id: tc.authorID, Name: tc.name})
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

	t.Run("test author not found", func(t *testing.T) {
		t.Parallel()
		tcs := []testCase{
			{
				authorID:    uuid.NewString(),
				name:        "new name",
				errExpected: nil,
			},
			{
				authorID:    uuid.NewString(),
				name:        "not found author",
				errExpected: entity.ErrAuthorNotFound,
			},
		}

		for _, tc := range tcs {
			libAuthors.EXPECT().ChangeAuthorInfo(ctx, tc.authorID, tc.name).Return(tc.errExpected)

			resp, err := impl.ChangeAuthorInfo(ctx, &library.ChangeAuthorInfoRequest{Id: tc.authorID, Name: tc.name})
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

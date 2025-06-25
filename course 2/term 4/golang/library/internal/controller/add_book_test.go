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

func TestAddBook(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libBooks := mocks.NewMockBookUseCase(ctrl)
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(nil, libBooks, libAuthors)

	type testCase struct {
		name          string
		authorIDs     []string
		errorExpected error
	}

	tcs := []testCase{
		{
			name:          "book",
			authorIDs:     []string{},
			errorExpected: nil,
		},
		{
			name:          "book",
			authorIDs:     []string{uuid.NewString(), uuid.NewString()},
			errorExpected: nil,
		},
		{
			name:          "book",
			authorIDs:     []string{"notExist"},
			errorExpected: entity.ErrAuthorNotFound,
		},
	}

	for _, tc := range tcs {
		for _, id := range tc.authorIDs {
			if tc.errorExpected != nil {
				libAuthors.EXPECT().GetAuthor(ctx, id).Return(entity.Author{}, tc.errorExpected)
			} else {
				libAuthors.EXPECT().GetAuthor(ctx, id).Return(entity.Author{Name: "author", ID: id}, nil)
			}
		}

		if tc.errorExpected == nil {
			libBooks.EXPECT().RegisterBook(ctx, tc.name, tc.authorIDs).Return(entity.Book{Name: tc.name, AuthorIDs: tc.authorIDs}, nil)
		}

		resp, err := impl.AddBook(ctx, &library.AddBookRequest{Name: tc.name, AuthorIds: tc.authorIDs})

		if tc.errorExpected != nil {
			s, ok := status.FromError(err)
			require.True(t, ok)
			require.Equal(t, codes.NotFound, s.Code())
			require.Empty(t, resp)
		} else {
			require.Equal(t, resp.GetBook().GetName(), tc.name)
			require.Equal(t, resp.GetBook().GetAuthorId(), tc.authorIDs)
			require.NoError(t, err)
		}
	}
}

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
)

func TestRegisterAuthor(t *testing.T) {
	t.Parallel()
	ctrl := gomock.NewController(t)
	ctx := context.Background()
	libAuthors := mocks.NewMockAuthorUseCase(ctrl)

	impl := New(nil, nil, libAuthors)

	type testCase struct {
		name          string
		errorExpected error
	}

	tcs := []testCase{
		{
			name:          "author",
			errorExpected: nil,
		},
	}

	for _, tc := range tcs {
		if tc.errorExpected == nil {
			libAuthors.EXPECT().RegisterAuthor(ctx, tc.name).Return(entity.Author{Name: tc.name, ID: uuid.NewString()}, nil)
		}

		resp, err := impl.RegisterAuthor(ctx, &library.RegisterAuthorRequest{Name: tc.name})

		require.NotEmpty(t, resp.GetId())
		require.NoError(t, err)
	}
}

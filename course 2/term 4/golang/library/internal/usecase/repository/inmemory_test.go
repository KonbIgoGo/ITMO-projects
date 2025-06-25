package repository

import (
	"context"
	"math/rand/v2"
	"strconv"
	"testing"

	"github.com/google/uuid"
	"github.com/project/library/internal/entity"
	"github.com/stretchr/testify/require"
)

func TestInmemory(t *testing.T) {
	t.Parallel()
	ctx := context.Background()
	var repo = NewInMemoryRepository()
	var authors []entity.Author = make([]entity.Author, 10)
	var books []entity.Book = make([]entity.Book, 10)

	t.Run("AuthorRepositoryTests", func(t *testing.T) {
		t.Parallel()
		t.Run("RegisterAuthorTest", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				for i := 0; i < 10; i++ {
					testCase := entity.Author{ID: uuid.New().String(), Name: "Test " + strconv.Itoa(rand.Int())}
					author, err := repo.CreateAuthor(ctx, testCase)

					require.Equal(t, testCase, author)
					require.NoError(t, err)
					authors[i] = testCase
				}
			})
			t.Run("fail", func(t *testing.T) {
				for i := 0; i < 10; i++ {
					id := authors[i].ID
					testCase := entity.Author{ID: id, Name: "Test " + strconv.Itoa(rand.Int())}
					author, err := repo.CreateAuthor(ctx, testCase)

					require.Empty(t, author)
					require.ErrorIs(t, err, entity.ErrAuthorAlreadyExists)
				}
			})
		})
		t.Run("GetAuthorTest", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				for i := 0; i < 10; i++ {
					author, err := repo.GetAuthor(ctx, authors[i].ID)

					require.NoError(t, err)
					require.Equal(t, authors[i], author)
				}
			})
			t.Run("fail", func(t *testing.T) {
				author, err := repo.GetAuthor(ctx, "FAILID")

				require.ErrorIs(t, err, entity.ErrAuthorNotFound)
				require.Empty(t, author)
			})
		})
		t.Run("ChangeAuthorInfoTest", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				err := repo.ChangeAuthorInfo(ctx, authors[0].ID, "Changed Name")
				require.NoError(t, err)

				updated, err := repo.GetAuthor(ctx, authors[0].ID)
				require.NoError(t, err)
				require.Equal(t, "Changed Name", updated.Name)
			})

			t.Run("fail", func(t *testing.T) {
				err := repo.ChangeAuthorInfo(ctx, "FAILID", "Changed Name")
				require.ErrorIs(t, err, entity.ErrAuthorNotFound)
			})
		})
	})
	//nolint:paralleltest // shared resources
	t.Run("BookRepositoryTests", func(t *testing.T) {
		t.Run("CreateBook", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				for i := 0; i < 10; i++ {
					testCase := entity.Book{ID: uuid.New().String(), Name: "testbook " + strconv.Itoa(rand.Int()), AuthorIDs: []string{authors[i].ID, authors[9-i].ID}}
					book, err := repo.CreateBook(ctx, testCase)
					require.NoError(t, err)
					require.Equal(t, testCase, book)

					books[i] = testCase
				}
			})
			t.Run("fail", func(t *testing.T) {
				book, err := repo.CreateBook(ctx, entity.Book{ID: books[0].ID, Name: "", AuthorIDs: []string{}})
				require.ErrorIs(t, err, entity.ErrBookAlreadyExists)
				require.Empty(t, book)
			})
		})
		t.Run("GetBookTest", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				for i := 0; i < 10; i++ {
					book, err := repo.GetBook(ctx, books[i].ID)

					require.NoError(t, err)
					require.Equal(t, books[i], book)
				}
			})
			t.Run("fail", func(t *testing.T) {
				author, err := repo.GetBook(ctx, "FAILID")

				require.ErrorIs(t, err, entity.ErrBookNotFound)
				require.Empty(t, author)
			})
		})
		t.Run("UpdateBookTest", func(t *testing.T) {
			t.Run("success", func(t *testing.T) {
				err := repo.UpdateBook(ctx, books[0].ID, "Changed Name", []string{})
				require.NoError(t, err)

				updated, err := repo.GetBook(ctx, books[0].ID)
				require.NoError(t, err)
				require.Equal(t, "Changed Name", updated.Name)
				require.Empty(t, updated.AuthorIDs)
			})
			t.Run("fail", func(t *testing.T) {
				err := repo.UpdateBook(ctx, "FAILID", "Changed Name", []string{})
				require.ErrorIs(t, err, entity.ErrAuthorNotFound)
			})
		})
	})
}

package library

import (
	"context"

	"github.com/project/library/internal/entity"
	"github.com/project/library/internal/usecase/repository"
	"go.uber.org/zap"
)

//go:generate go tool go.uber.org/mock/mockgen -source=./interfaces.go -destination=../../mocks/library_mock.go -package=mocks
type (
	AuthorUseCase interface {
		RegisterAuthor(ctx context.Context, authorName string) (entity.Author, error)
		GetAuthor(ctx context.Context, ID string) (entity.Author, error)
		ChangeAuthorInfo(ctx context.Context, ID string, name string) error
		CollectAuthorBooks(ctx context.Context, author entity.Author) ([]entity.Book, error)
	}

	BookUseCase interface {
		RegisterBook(ctx context.Context, name string, authorIDs []string) (entity.Book, error)
		GetBook(ctx context.Context, bookID string) (entity.Book, error)
		UpdateBook(ctx context.Context, ID string, name string, authorIDs []string) error
	}
)

var _ AuthorUseCase = (*libraryImpl)(nil)
var _ BookUseCase = (*libraryImpl)(nil)

type libraryImpl struct {
	logger           *zap.Logger
	authorRepository repository.AuthorRepository
	booksRepository  repository.BookRepository
	outboxRepository repository.OutboxRepository
	transacator      repository.Transactor
}

func New(
	logger *zap.Logger,
	authorRepository repository.AuthorRepository,
	booksRepository repository.BookRepository,
	outboxRepository repository.OutboxRepository,
	transacator repository.Transactor,
) *libraryImpl {
	return &libraryImpl{
		logger:           logger,
		authorRepository: authorRepository,
		booksRepository:  booksRepository,
		outboxRepository: outboxRepository,
		transacator:      transacator,
	}
}

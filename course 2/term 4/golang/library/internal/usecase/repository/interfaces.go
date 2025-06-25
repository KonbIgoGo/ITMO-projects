package repository

import (
	"context"
	"time"

	"github.com/project/library/internal/entity"
)

//go:generate go tool go.uber.org/mock/mockgen -source=./interfaces.go -destination=../../mocks/repo_mock.go -package=mocks
type (
	AuthorRepository interface {
		CreateAuthor(ctx context.Context, author entity.Author) (entity.Author, error)
		GetAuthor(ctx context.Context, ID string) (entity.Author, error)
		ChangeAuthorInfo(ctx context.Context, ID string, name string) error
	}

	BookRepository interface {
		CreateBook(ctx context.Context, book entity.Book) (entity.Book, error)
		GetBook(ctx context.Context, bookID string) (entity.Book, error)
		UpdateBook(ctx context.Context, bookID string, name string, authorIDs []string) error
		GetAuthorBooks(ctx context.Context, authorID string) ([]entity.Book, error)
	}

	OutboxRepository interface {
		SendMessage(ctx context.Context, idempotencyKey string, kind OutboxKind, msg []byte) error
		GetMessages(ctx context.Context, batchSize int, inProgressTTL time.Duration) ([]OutboxData, error)
		MarkAsProcessed(ctx context.Context, idempotencyKeys []string) error
	}

	OutboxData struct {
		IdempotencyKey string
		Kind           OutboxKind
		RawData        []byte
	}
)

type OutboxKind int

const (
	OutboxKindUndefined OutboxKind = iota
	OutboxKindBook      OutboxKind = 1
	OutboxKindAuthor    OutboxKind = 2
)

func (o OutboxKind) String() string {
	switch o {
	case OutboxKindBook:
		return "book"
	case OutboxKindAuthor:
		return "author"
	default:
		return "undefined"
	}
}

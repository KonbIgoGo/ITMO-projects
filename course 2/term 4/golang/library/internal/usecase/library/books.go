package library

import (
	"context"
	"encoding/json"

	"github.com/google/uuid"
	"github.com/project/library/internal/entity"
	"github.com/project/library/internal/usecase/repository"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
	"go.uber.org/zap"
)

func (l *libraryImpl) RegisterBook(ctx context.Context, name string, authorIDs []string) (entity.Book, error) {
	var book entity.Book

	span := trace.SpanFromContext(ctx)
	l.logger.Info("start to register book",
		zap.String("component", "library"),
		zap.String("operation", "register book"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		var txErr error
		book, txErr = l.booksRepository.CreateBook(ctx, entity.Book{
			ID:        uuid.New().String(),
			Name:      name,
			AuthorIDs: authorIDs,
		})

		if txErr != nil {
			l.logger.Error("txErr", zap.Error(txErr))
			return txErr
		}

		serialized, txErr := json.Marshal(book)

		if txErr != nil {
			l.logger.Error("txErr", zap.Error(txErr))
			return txErr
		}

		idempotencyKey := repository.OutboxKindBook.String() + "_" + "register" + "_" + book.ID
		txErr = l.outboxRepository.SendMessage(ctx, idempotencyKey, repository.OutboxKindBook, serialized)

		if txErr != nil {
			l.logger.Error("txErr", zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		return entity.Book{}, err
	}

	span.SetAttributes(attribute.String("book.id", book.ID))
	l.logger.Info("book registred",
		zap.String("component", "library"),
		zap.String("operation", "register book"),
		zap.String("book_id", book.ID),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return book, nil
}

func (l *libraryImpl) GetBook(ctx context.Context, bookID string) (entity.Book, error) {
	span := trace.SpanFromContext(ctx)
	l.logger.Info("start to recieving book",
		zap.String("component", "library"),
		zap.String("operation", "get book"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	var book entity.Book
	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		var txErr error
		book, txErr = l.booksRepository.GetBook(ctx, bookID)
		if txErr != nil {
			l.logger.Error("txErr", zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		return entity.Book{}, err
	}

	span.SetAttributes(attribute.String("book.id", book.ID))
	l.logger.Info("book recieved",
		zap.String("component", "library"),
		zap.String("operation", "get book"),
		zap.String("book_id", book.ID),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return book, err
}

func (l *libraryImpl) UpdateBook(ctx context.Context, id string, name string, authorIDs []string) error {
	span := trace.SpanFromContext(ctx)
	l.logger.Info("start to updating book",
		zap.String("component", "library"),
		zap.String("operation", "update book"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		txErr := l.booksRepository.UpdateBook(ctx, id, name, authorIDs)
		if txErr != nil {
			l.logger.Error("txErr", zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		l.logger.Error("update book failed", zap.Error(err))
	}

	span.SetAttributes(attribute.String("book.id", id))
	l.logger.Info("book updated",
		zap.String("component", "library"),
		zap.String("operation", "update book"),
		zap.String("book_id", id),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return nil
}

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

func (l *libraryImpl) RegisterAuthor(ctx context.Context, authorName string) (entity.Author, error) {
	var author entity.Author

	span := trace.SpanFromContext(ctx)
	l.logger.Info("start to register author",
		zap.String("component", "library"),
		zap.String("operation", "register author"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		var txErr error
		author, txErr = l.authorRepository.CreateAuthor(ctx, entity.Author{
			ID:   uuid.New().String(),
			Name: authorName,
		})

		if txErr != nil {
			l.logger.Error("txError", zap.Error(txErr))
			return txErr
		}

		serialized, txErr := json.Marshal(author)

		if txErr != nil {
			l.logger.Error("marshallError", zap.Error(txErr))
			return txErr
		}

		idempotencyKey := repository.OutboxKindAuthor.String() + "_" + "register" + "_" + author.ID
		txErr = l.outboxRepository.SendMessage(ctx, idempotencyKey, repository.OutboxKindAuthor, serialized)
		if txErr != nil {
			l.logger.Error("SendMsgError", zap.Error(txErr))
			return txErr
		}
		return nil
	})

	if err != nil {
		l.logger.Error("failed to register author", zap.Error(err))
		return entity.Author{}, err
	}

	span.SetAttributes(attribute.String("author.id", author.ID))
	l.logger.Info("author registred",
		zap.String("component", "library"),
		zap.String("operation", "register author"),
		zap.String("author_id", author.ID),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return author, nil
}

func (l *libraryImpl) GetAuthor(ctx context.Context, id string) (entity.Author, error) {
	var author entity.Author

	span := trace.SpanFromContext(ctx)
	l.logger.Info("start to recieve author",
		zap.String("component", "library"),
		zap.String("operation", "get author"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		var txErr error
		author, txErr = l.authorRepository.GetAuthor(ctx, id)

		if txErr != nil {
			l.logger.Error("txError "+id, zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		l.logger.Error("failed to recieve author info", zap.Error(err))
		return entity.Author{}, err
	}

	span.SetAttributes(attribute.String("author.id", author.ID))
	l.logger.Info("author recieved",
		zap.String("component", "library"),
		zap.String("operation", "get author"),
		zap.String("author_id", author.ID),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return author, nil
}

func (l *libraryImpl) ChangeAuthorInfo(ctx context.Context, id string, name string) (txErr error) {
	span := trace.SpanFromContext(ctx)
	l.logger.Info("start changing author info",
		zap.String("component", "library"),
		zap.String("operation", "change author info"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)

	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		txErr := l.authorRepository.ChangeAuthorInfo(ctx, id, name)

		if txErr != nil {
			l.logger.Error("txError", zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		l.logger.Error("failed to change author info", zap.Error(err))
		return err
	}

	span.SetAttributes(attribute.String("author.id", id))
	l.logger.Info("author info changed",
		zap.String("component", "library"),
		zap.String("operation", "change author info"),
		zap.String("author_id", id),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return nil
}

func (l *libraryImpl) CollectAuthorBooks(ctx context.Context, author entity.Author) ([]entity.Book, error) {
	var books []entity.Book

	span := trace.SpanFromContext(ctx)
	l.logger.Info("start collecting author books",
		zap.String("component", "library"),
		zap.String("operation", "collect author books"),
		zap.String("trace_id", span.SpanContext().TraceID().String()),
	)
	defer span.End()

	err := l.transacator.WithTx(ctx, func(ctx context.Context) error {
		var txErr error
		books, txErr = l.booksRepository.GetAuthorBooks(ctx, author.ID)

		if txErr != nil {
			l.logger.Error("txError", zap.Error(txErr))
			return txErr
		}

		return nil
	})

	if err != nil {
		l.logger.Error("failed to collect author books", zap.Error(err))
		return nil, err
	}

	span.SetAttributes(attribute.String("author.id", author.ID))
	l.logger.Info("author books collected",
		zap.String("component", "library"),
		zap.String("operation", "collect author books"),
		zap.String("author_id", author.ID),
		zap.String("trace_id", span.SpanContext().SpanID().String()),
	)

	return books, nil
}

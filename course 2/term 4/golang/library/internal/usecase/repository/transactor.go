package repository

import (
	"context"
	"errors"
	"fmt"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/sirupsen/logrus"
)

//go:generate go tool go.uber.org/mock/mockgen -source=./transactor.go -destination=../../mocks/transactor_mock.go -package=mocks
type Transactor interface {
	WithTx(ctx context.Context, function func(ctx context.Context) error) error
}

var _ Transactor = (*transactorImpl)(nil)

type transactorImpl struct {
	db *pgxpool.Pool
}

func NewTransactor(db *pgxpool.Pool) *transactorImpl {
	return &transactorImpl{
		db: db,
	}
}
func (t *transactorImpl) WithTx(ctx context.Context, function func(ctx context.Context) error) (txErr error) {
	ctxWithTx, tx, err := injectTx(ctx, t.db)

	if err != nil {
		return fmt.Errorf("can not inject transaction, error: %w", err)
	}

	defer func() {
		if txErr != nil {
			err = tx.Rollback(ctxWithTx)
			if err != nil {
				logrus.Debug("Failed to rollback transaction")
			}
			return
		}

		err = tx.Commit(ctxWithTx)
		if err != nil {
			logrus.Debug("Failed to commit transaction")
		}
	}()

	err = function(ctxWithTx)

	if err != nil {
		return fmt.Errorf("function execution error: %w", err)
	}

	return nil
}

type txInjector struct{}

var ErrTxNotFound = errors.New("tx not found in context")

func injectTx(ctx context.Context, pool *pgxpool.Pool) (context.Context, pgx.Tx, error) {
	if tx, err := extractTx(ctx); err == nil {
		return ctx, tx, nil
	}

	tx, err := pool.Begin(ctx)

	if err != nil {
		return nil, nil, err
	}

	return context.WithValue(ctx, txInjector{}, tx), tx, nil
}

func extractTx(ctx context.Context) (pgx.Tx, error) {
	tx, ok := ctx.Value(txInjector{}).(pgx.Tx)

	if !ok {
		return nil, ErrTxNotFound
	}

	return tx, nil
}

package outbox

import (
	"context"
	"errors"
	"fmt"
	"testing"
	"time"

	"github.com/project/library/config"
	"github.com/project/library/internal/mocks"
	"github.com/project/library/internal/usecase/repository"
	"github.com/stretchr/testify/require"
	"go.uber.org/mock/gomock"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"go.uber.org/zap/zaptest/observer"
)

func globalOutboxHandler(errExpected bool) GlobalHandler {
	return func(kind repository.OutboxKind) (KindHandler, error) {
		switch kind {
		case repository.OutboxKindBook:
			return outboxHandler(errExpected), nil
		default:
			return nil, fmt.Errorf("unsupported outbox kind: %d", kind)
		}
	}
}

func outboxHandler(errExpected bool) KindHandler {
	return func(ctx context.Context, data []byte) error {
		if errExpected {
			return errors.New("handler error")
		}
		return nil
	}
}

var _ repository.Transactor = (*idempotentTransactor)(nil)

type idempotentTransactor struct{}

func (i *idempotentTransactor) WithTx(ctx context.Context, function func(ctx context.Context) error) error {
	return function(ctx)
}

func TestOutbox(t *testing.T) {
	t.Parallel()
	cfg := &config.Config{}
	cfg.Outbox.Enabled = true
	ctx := context.Background()
	t.Run("transactor error test", func(t *testing.T) {
		t.Parallel()

		type testCase struct {
			transactorErrExpected bool
		}

		tcs := []testCase{
			{
				transactorErrExpected: false,
			},
			{
				transactorErrExpected: true,
			},
		}

		for _, tc := range tcs {
			core, logs := observer.New(zapcore.ErrorLevel)
			logger := zap.New(core)

			ctrl := gomock.NewController(t)
			defer ctrl.Finish()

			transactor := mocks.NewMockTransactor(ctrl)

			outbox := New(logger, nil, nil, cfg, transactor)

			if tc.transactorErrExpected {
				transactor.
					EXPECT().
					WithTx(ctx, gomock.Any()).
					Return(errors.New("transactor failure")).
					AnyTimes()
			} else {
				transactor.
					EXPECT().
					WithTx(ctx, gomock.Any()).
					Return(nil).
					AnyTimes()
			}

			outbox.Start(ctx, 1, 1, time.Millisecond, time.Hour)
			time.Sleep(100 * time.Millisecond)

			if tc.transactorErrExpected {
				require.NotEmpty(t, logs.All())
			} else {
				require.Empty(t, logs.All())
			}
		}
	})

	t.Run("outbox function test", func(t *testing.T) {
		t.Parallel()

		type testCase struct {
			getMsgErrExpected         bool
			unexpectedKindErrExpected bool
			handlerErrExpected        bool
			processedErrExpected      bool
		}

		tcs := []testCase{
			{
				getMsgErrExpected:         false,
				unexpectedKindErrExpected: false,
				handlerErrExpected:        false,
				processedErrExpected:      false,
			},
			{
				getMsgErrExpected:         true,
				unexpectedKindErrExpected: false,
				handlerErrExpected:        false,
				processedErrExpected:      false,
			},
			{
				getMsgErrExpected:         false,
				unexpectedKindErrExpected: true,
				handlerErrExpected:        false,
				processedErrExpected:      false,
			},
			{
				getMsgErrExpected:         false,
				unexpectedKindErrExpected: false,
				handlerErrExpected:        true,
				processedErrExpected:      false,
			},
			{
				getMsgErrExpected:         false,
				unexpectedKindErrExpected: false,
				handlerErrExpected:        false,
				processedErrExpected:      true,
			},
		}

		for _, tc := range tcs {
			core, logs := observer.New(zapcore.ErrorLevel)
			logger := zap.New(core)

			ctrl := gomock.NewController(t)
			defer ctrl.Finish()
			outboxRepo := mocks.NewMockOutboxRepository(ctrl)
			transactor := &idempotentTransactor{}
			globalHandler := globalOutboxHandler(tc.handlerErrExpected)
			outbox := New(logger, outboxRepo, globalHandler, cfg, transactor)

			switch {
			case tc.getMsgErrExpected:
				outboxRepo.EXPECT().GetMessages(ctx, gomock.Any(), gomock.Any()).Return(nil, errors.New("getMsgError")).AnyTimes()
			case tc.unexpectedKindErrExpected:
				outboxRepo.EXPECT().GetMessages(ctx, gomock.Any(), gomock.Any()).Return([]repository.OutboxData{
					{
						IdempotencyKey: "1",
						Kind:           -1,
						RawData:        nil,
					},
				}, nil).AnyTimes()
				outboxRepo.EXPECT().MarkAsProcessed(ctx, gomock.Any()).Return(nil).AnyTimes()
			case tc.processedErrExpected:
				outboxRepo.EXPECT().GetMessages(ctx, gomock.Any(), gomock.Any()).Return([]repository.OutboxData{
					{
						IdempotencyKey: "1",
						Kind:           1,
						RawData:        nil,
					},
				}, nil).AnyTimes()

				outboxRepo.EXPECT().MarkAsProcessed(ctx, gomock.Any()).Return(errors.New("mark processed error")).AnyTimes()
			default:
				outboxRepo.EXPECT().GetMessages(ctx, gomock.Any(), gomock.Any()).Return([]repository.OutboxData{
					{
						IdempotencyKey: "1",
						Kind:           1,
						RawData:        nil,
					},
				}, nil).AnyTimes()
				outboxRepo.EXPECT().MarkAsProcessed(ctx, gomock.Any()).Return(nil).AnyTimes()
			}

			outbox.Start(ctx, 1, 1, 0, time.Hour)
			time.Sleep(time.Second)

			if tc.getMsgErrExpected || tc.handlerErrExpected || tc.processedErrExpected || tc.unexpectedKindErrExpected {
				require.NotEmpty(t, logs.All())
			} else {
				require.Empty(t, logs.All())
			}
		}
	})
}

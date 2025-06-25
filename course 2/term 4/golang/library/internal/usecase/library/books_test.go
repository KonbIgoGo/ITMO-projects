package library

import (
	"context"
	"errors"
	"testing"

	"github.com/project/library/internal/entity"
	"github.com/project/library/internal/mocks"
	"github.com/stretchr/testify/require"
	"go.uber.org/mock/gomock"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"go.uber.org/zap/zaptest/observer"
)

func TestBooksLibrary(t *testing.T) {
	t.Parallel()
	ctx := context.Background()

	t.Run("transactor error test", func(t *testing.T) {
		type testCase struct {
			errExpected bool
		}
		tcs := []testCase{
			{
				errExpected: false,
			},
			{
				errExpected: true,
			},
		}

		t.Run("register book test", func(t *testing.T) {
			t.Parallel()
			for _, tc := range tcs {
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				transactor := mocks.NewMockTransactor(ctrl)

				useCase := New(nil, authorRepo, bookRepo, nil, transactor)

				if tc.errExpected {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(errors.New("transactor error"))
				} else {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(nil)
				}

				_, err := useCase.RegisterBook(ctx, "name", nil)
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})

		t.Run("get book test", func(t *testing.T) {
			t.Parallel()
			for _, tc := range tcs {
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				transactor := mocks.NewMockTransactor(ctrl)

				useCase := New(nil, authorRepo, bookRepo, nil, transactor)

				if tc.errExpected {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(errors.New("transactor error"))
				} else {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(nil)
				}

				_, err := useCase.GetBook(ctx, "id")
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})
		t.Run("change book info test", func(t *testing.T) {
			t.Parallel()
			for _, tc := range tcs {
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				transactor := mocks.NewMockTransactor(ctrl)

				useCase := New(nil, authorRepo, bookRepo, nil, transactor)

				if tc.errExpected {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(errors.New("transactor error"))
				} else {
					transactor.EXPECT().WithTx(ctx, gomock.Any()).Return(nil)
				}

				err := useCase.UpdateBook(ctx, "id", "name", nil)
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})
	})

	t.Run("idempotent transactor test", func(t *testing.T) {
		t.Parallel()

		type testCase struct {
			repositoryErrExpected bool
			sendMsgErrorExpected  bool
		}

		tcs := []testCase{
			{
				repositoryErrExpected: false,
				sendMsgErrorExpected:  false,
			},
			{
				repositoryErrExpected: true,
				sendMsgErrorExpected:  false,
			},
			{
				repositoryErrExpected: false,
				sendMsgErrorExpected:  true,
			},
		}

		t.Run("register book test", func(t *testing.T) {
			t.Parallel()

			for _, tc := range tcs {
				core, logs := observer.New(zapcore.ErrorLevel)
				logger := zap.New(core)
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				outboxRepo := mocks.NewMockOutboxRepository(ctrl)

				useCase := New(logger, authorRepo, bookRepo, outboxRepo, &idempotentTransactor{})
				switch {
				case tc.repositoryErrExpected:
					bookRepo.EXPECT().CreateBook(ctx, gomock.Any()).Return(entity.Book{}, errors.New("repository error"))
				case tc.sendMsgErrorExpected:
					bookRepo.EXPECT().CreateBook(ctx, gomock.Any()).Return(entity.Book{}, nil)
					outboxRepo.EXPECT().SendMessage(ctx, gomock.Any(), gomock.Any(), gomock.Any()).Return(errors.New("send msg error"))
				default:
					bookRepo.EXPECT().CreateBook(ctx, gomock.Any()).Return(entity.Book{}, nil)
					outboxRepo.EXPECT().SendMessage(ctx, gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
				}

				_, err := useCase.RegisterBook(ctx, "name", nil)
				if tc.repositoryErrExpected || tc.sendMsgErrorExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})

		t.Run("get book test", func(t *testing.T) {
			t.Parallel()

			for _, tc := range tcs {
				core, logs := observer.New(zapcore.ErrorLevel)
				logger := zap.New(core)
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				outboxRepo := mocks.NewMockOutboxRepository(ctrl)

				useCase := New(logger, authorRepo, bookRepo, outboxRepo, &idempotentTransactor{})
				if tc.repositoryErrExpected {
					bookRepo.EXPECT().GetBook(ctx, gomock.Any()).Return(entity.Book{}, errors.New("repository error"))
				} else {
					bookRepo.EXPECT().GetBook(ctx, gomock.Any()).Return(entity.Book{}, nil)
				}

				_, err := useCase.GetBook(ctx, "id")
				if tc.repositoryErrExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})

		t.Run("change book info test", func(t *testing.T) {
			t.Parallel()

			for _, tc := range tcs {
				core, logs := observer.New(zapcore.ErrorLevel)
				logger := zap.New(core)
				ctrl := gomock.NewController(t)
				authorRepo := mocks.NewMockAuthorRepository(ctrl)
				bookRepo := mocks.NewMockBookRepository(ctrl)
				outboxRepo := mocks.NewMockOutboxRepository(ctrl)

				useCase := New(logger, authorRepo, bookRepo, outboxRepo, &idempotentTransactor{})
				if tc.repositoryErrExpected {
					bookRepo.EXPECT().UpdateBook(ctx, gomock.Any(), gomock.Any(), gomock.Any()).Return(errors.New("repository error"))
				} else {
					bookRepo.EXPECT().UpdateBook(ctx, gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
				}

				err := useCase.UpdateBook(ctx, "id", "name", nil)
				if tc.repositoryErrExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})
	})
}

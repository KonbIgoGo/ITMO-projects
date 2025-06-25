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

type idempotentTransactor struct{}

func (i *idempotentTransactor) WithTx(ctx context.Context, function func(ctx context.Context) error) error {
	return function(ctx)
}

func TestAuthorLibrary(t *testing.T) {
	t.Parallel()
	ctx := context.Background()

	t.Run("transactor error test", func(t *testing.T) {
		t.Parallel()

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

		t.Run("collect author books test", func(t *testing.T) {
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

				_, err := useCase.CollectAuthorBooks(ctx, entity.Author{})
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})

		t.Run("get author test", func(t *testing.T) {
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

				_, err := useCase.GetAuthor(ctx, "id")
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})

		t.Run("register author test", func(t *testing.T) {
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

				_, err := useCase.RegisterAuthor(ctx, "name")
				if tc.errExpected {
					require.Error(t, err)
				} else {
					require.NoError(t, err)
				}
			}
		})

		t.Run("change author info test", func(t *testing.T) {
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

				err := useCase.ChangeAuthorInfo(ctx, "id", "name")
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

		t.Run("collect author books test", func(t *testing.T) {
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
					bookRepo.EXPECT().GetAuthorBooks(ctx, gomock.Any()).Return(nil, errors.New("repository error"))
				} else {
					bookRepo.EXPECT().GetAuthorBooks(ctx, gomock.Any()).Return(nil, nil)
				}

				_, err := useCase.CollectAuthorBooks(ctx, entity.Author{})
				if tc.repositoryErrExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})
		t.Run("get author test", func(t *testing.T) {
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
					authorRepo.EXPECT().GetAuthor(ctx, gomock.Any()).Return(entity.Author{}, errors.New("repository error"))
				} else {
					authorRepo.EXPECT().GetAuthor(ctx, gomock.Any()).Return(entity.Author{}, nil)
				}

				_, err := useCase.GetAuthor(ctx, "id")
				if tc.repositoryErrExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})
		t.Run("register author test", func(t *testing.T) {
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
					authorRepo.
						EXPECT().
						CreateAuthor(ctx, gomock.Any()).
						Return(entity.Author{}, errors.New("repository error"))

				case tc.sendMsgErrorExpected:
					authorRepo.
						EXPECT().
						CreateAuthor(ctx, gomock.Any()).
						Return(entity.Author{}, nil)
					outboxRepo.
						EXPECT().
						SendMessage(ctx, gomock.Any(), gomock.Any(), gomock.Any()).
						Return(errors.New("send msg error"))

				default:
					authorRepo.
						EXPECT().
						CreateAuthor(ctx, gomock.Any()).
						Return(entity.Author{}, nil)
					outboxRepo.
						EXPECT().
						SendMessage(ctx, gomock.Any(), gomock.Any(), gomock.Any()).
						Return(nil)
				}

				_, err := useCase.RegisterAuthor(ctx, "name")
				if tc.repositoryErrExpected || tc.sendMsgErrorExpected {
					require.Error(t, err)
					require.NotEmpty(t, logs.All())
				} else {
					require.NoError(t, err)
					require.Empty(t, logs.All())
				}
			}
		})
		t.Run("change author info test", func(t *testing.T) {
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
					authorRepo.EXPECT().ChangeAuthorInfo(ctx, gomock.Any(), gomock.Any()).Return(errors.New("repository error"))
				} else {
					authorRepo.EXPECT().ChangeAuthorInfo(ctx, gomock.Any(), gomock.Any()).Return(nil)
				}

				err := useCase.ChangeAuthorInfo(ctx, "id", "name")
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

package controller

import (
	"context"
	"errors"

	"github.com/project/library/internal/entity"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func (i *implementation) convertErr(err error) error {
	switch {
	case errors.Is(err, entity.ErrBookNotFound) || errors.Is(err, entity.ErrAuthorNotFound):
		return status.Error(codes.NotFound, err.Error())
	default:
		return status.Error(codes.Internal, err.Error())
	}
}

func (i *implementation) validateAuthors(ctx context.Context, authorIDs []string) error {
	for _, id := range authorIDs {
		if _, err := i.authorUseCase.GetAuthor(ctx, id); err != nil {
			return err
		}
	}

	return nil
}

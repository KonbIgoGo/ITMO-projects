package repository

import (
	"context"
	"slices"
	"sync"

	"github.com/project/library/internal/entity"
)

var _ AuthorRepository = (*inMemoryImpl)(nil)
var _ BookRepository = (*inMemoryImpl)(nil)

type inMemoryImpl struct {
	authorsMx *sync.RWMutex
	authors   map[string]*entity.Author

	booksMx *sync.RWMutex
	books   map[string]*entity.Book
}

func NewInMemoryRepository() *inMemoryImpl {
	return &inMemoryImpl{
		authorsMx: new(sync.RWMutex),
		authors:   make(map[string]*entity.Author),

		booksMx: new(sync.RWMutex),
		books:   make(map[string]*entity.Book),
	}
}

func (i *inMemoryImpl) CreateAuthor(_ context.Context, author entity.Author) (entity.Author, error) {
	i.authorsMx.Lock()
	defer i.authorsMx.Unlock()

	if _, ok := i.authors[author.ID]; ok {
		return entity.Author{}, entity.ErrAuthorAlreadyExists
	}

	i.authors[author.ID] = &author
	return author, nil
}

func (i *inMemoryImpl) GetAuthor(_ context.Context, id string) (entity.Author, error) {
	i.authorsMx.RLock()
	defer i.authorsMx.RUnlock()
	if author, ok := i.authors[id]; ok {
		return *author, nil
	}
	return entity.Author{}, entity.ErrAuthorNotFound
}

func (i *inMemoryImpl) ChangeAuthorInfo(_ context.Context, id string, name string) error {
	i.authorsMx.Lock()
	defer i.authorsMx.Unlock()

	if _, ok := i.authors[id]; !ok {
		return entity.ErrAuthorNotFound
	}
	i.authors[id].Name = name
	return nil
}

func (i *inMemoryImpl) CreateBook(_ context.Context, book entity.Book) (entity.Book, error) {
	i.booksMx.Lock()
	defer i.booksMx.Unlock()

	if _, ok := i.books[book.ID]; ok {
		return entity.Book{}, entity.ErrBookAlreadyExists
	}

	i.books[book.ID] = &book

	return book, nil
}
func (i *inMemoryImpl) GetBook(_ context.Context, bookID string) (entity.Book, error) {
	i.booksMx.RLock()
	defer i.booksMx.RUnlock()
	book, ok := i.books[bookID]
	if !ok {
		return entity.Book{}, entity.ErrBookNotFound
	}
	return *book, nil
}

func (i *inMemoryImpl) UpdateBook(_ context.Context, bookID string, name string, authorIDs []string) error {
	i.booksMx.Lock()
	defer i.booksMx.Unlock()

	if _, ok := i.books[bookID]; !ok {
		return entity.ErrAuthorNotFound
	}
	i.books[bookID].Name = name
	i.books[bookID].AuthorIDs = authorIDs
	return nil
}

func (i *inMemoryImpl) GetAuthorBooks(_ context.Context, authorID string) ([]entity.Book, error) {
	var res []entity.Book = make([]entity.Book, 0)
	for _, v := range i.books {
		if slices.Contains(v.AuthorIDs, authorID) {
			res = append(res, *v)
		}
	}
	return res, nil
}

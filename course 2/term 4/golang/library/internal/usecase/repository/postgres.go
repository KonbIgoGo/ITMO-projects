package repository

import (
	"context"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/project/library/internal/entity"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/sirupsen/logrus"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

var _ BookRepository = (*postgresRepository)(nil)
var _ AuthorRepository = (*postgresRepository)(nil)

type postgresRepository struct {
	db *pgxpool.Pool
}

func NewPostgresRepository(db *pgxpool.Pool) *postgresRepository {
	return &postgresRepository{db: db}
}

var CreateBookLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_create_book_latency",
	Help:    "Latency of CreateBook",
	Buckets: prometheus.DefBuckets,
})
var CreateAuthorLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_create_author_latency",
	Help:    "Latency of CreateAuthor",
	Buckets: prometheus.DefBuckets,
})
var GetBookLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_get_book_latency",
	Help:    "Latency of GetBook",
	Buckets: prometheus.DefBuckets,
})
var GetAuthorLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_get_author_latency",
	Help:    "Latency of GetAutho",
	Buckets: prometheus.DefBuckets,
})
var UpdateBookLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_update_book_latency",
	Help:    "Latency of UpdateBook",
	Buckets: prometheus.DefBuckets,
})
var UpdateAuthorLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_update_author_latency",
	Help:    "Latency of UpdateAuthor",
	Buckets: prometheus.DefBuckets,
})
var GetAuthorBooksLatency = prometheus.NewHistogram(prometheus.HistogramOpts{
	Name:    "database_get_author_books_latency",
	Help:    "Latency of GetAuthorBooks",
	Buckets: prometheus.DefBuckets,
})

func init() {
	prometheus.MustRegister(
		CreateBookLatency,
		CreateAuthorLatency,
		GetBookLatency,
		GetAuthorLatency,
		UpdateBookLatency,
		UpdateAuthorLatency,
		GetAuthorBooksLatency,
	)
}

func (p *postgresRepository) CreateAuthor(ctx context.Context, author entity.Author) (res entity.Author, txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		CreateAuthorLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return entity.Author{}, err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return entity.Author{}, err
	}

	const queryAuthor = /* sql */ `
	INSERT INTO author (id, name)
	VALUES ($1, $2)
	RETURNING registred_at, updated_at
	`

	res.ID = author.ID
	res.Name = author.Name

	if err = tx.QueryRow(ctx, queryAuthor, res.ID, res.Name).Scan(&res.RegistredAt, &res.UpdatedAt); err != nil {
		return entity.Author{}, err
	}

	span.SetAttributes(attribute.String("author.id", author.ID))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "register author",
		"author_id": author.ID,
		"trace_id":  span.SpanContext().TraceID().String(),
	})
	return
}

func (p *postgresRepository) GetAuthor(ctx context.Context, id string) (res entity.Author, txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()
	defer func() {
		GetAuthorLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return entity.Author{}, err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return entity.Author{}, err
	}

	const authorQuery = /* sql */ `
		SELECT id, name, registred_at, updated_at
		FROM author WHERE id = $1 FOR UPDATE
	`
	var author entity.Author

	if err = tx.QueryRow(ctx, authorQuery, id).Scan(&author.ID, &author.Name, &author.RegistredAt, &author.UpdatedAt); err != nil {
		return entity.Author{}, entity.ErrAuthorNotFound
	}
	span.SetAttributes(attribute.String("author.id", id))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "get author",
		"author_id": id,
		"trace_id":  span.SpanContext().TraceID().String(),
	})

	return author, nil
}

func (p *postgresRepository) ChangeAuthorInfo(ctx context.Context, id string, name string) (txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		UpdateAuthorLatency.Observe(time.Since(start).Seconds())
	}()
	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {

			return err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return err
	}

	const query = /* sql */ `
	UPDATE author
	SET name = $1
	WHERE id = $2
	`

	_, err = tx.Exec(ctx, query, name, id)
	if err != nil {
		return err
	}

	span.SetAttributes(attribute.String("author.id", id))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "change author info",
		"author_id": id,
		"trace_id":  span.SpanContext().TraceID().String(),
	})

	return nil
}

func (p *postgresRepository) CreateBook(ctx context.Context, book entity.Book) (resBook entity.Book, txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		CreateBookLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return entity.Book{}, err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return entity.Book{}, err
	}

	const queryBook = /* sql */ `
		INSERT INTO book (id, name)
		VALUES ($1,$2)
		RETURNING created_at, updated_at
		`

	result := entity.Book{
		ID:        book.ID,
		Name:      book.Name,
		AuthorIDs: book.AuthorIDs,
	}

	err = tx.QueryRow(ctx, queryBook, book.ID, book.Name).Scan(&result.CreatedAt, &result.UpdatedAt)
	if err != nil {
		return entity.Book{}, err
	}

	if len(book.AuthorIDs) > 0 {
		rows := make([][]interface{}, len(book.AuthorIDs))
		for i, authorID := range book.AuthorIDs {
			rows[i] = []interface{}{authorID, book.ID}
		}

		_, err = tx.CopyFrom(
			ctx,
			pgx.Identifier{"author_book"},
			[]string{"author_id", "book_id"},
			pgx.CopyFromRows(rows))

		if err != nil {
			return entity.Book{}, err
		}
	}

	span.SetAttributes(attribute.String("book.id", book.ID))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "register book",
		"book_id":   book.ID,
		"trace_id":  span.SpanContext().TraceID().String(),
	})
	return result, nil
}

func (p *postgresRepository) GetBook(ctx context.Context, bookID string) (resBook entity.Book, txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		GetBookLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return entity.Book{}, err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return entity.Book{}, err
	}

	const queryBook = /* sql */ `
	SELECT id, name, created_at, updated_at
	FROM book WHERE id = $1 FOR UPDATE
	`

	var book entity.Book
	if err = tx.QueryRow(ctx, queryBook, bookID).Scan(&book.ID, &book.Name, &book.CreatedAt, &book.UpdatedAt); err != nil {
		return entity.Book{}, entity.ErrBookNotFound
	}

	const queryAuthors = /* sql */ `
	SELECT author_id
	FROM author_book
	WHERE book_id = $1 FOR UPDATE`

	rows, err := tx.Query(ctx, queryAuthors, bookID)
	if err != nil {
		return entity.Book{}, entity.ErrAuthorNotFound
	}

	authorIDs, err := pgx.CollectRows(rows, func(row pgx.CollectableRow) (string, error) {
		var authorID string
		if err = row.Scan(&authorID); err != nil {
			return "", err
		}
		return authorID, nil
	})
	if err != nil {
		return entity.Book{}, err
	}
	book.AuthorIDs = authorIDs

	span.SetAttributes(attribute.String("book.id", bookID))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "get book",
		"book_id":   bookID,
		"trace_id":  span.SpanContext().TraceID().String(),
	})
	return book, nil
}

func (p *postgresRepository) UpdateBook(ctx context.Context, bookID string, name string, authorIDs []string) (txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		UpdateBookLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return err
	}

	const updateBookQuery = /* sql */ `
	UPDATE book
	SET 
		name = $1,
		updated_at = $2
	WHERE id = $3
	`

	clearAuthorsQuery := /* sql */ `
	DELETE FROM author_book
	WHERE book_id = $1
	AND author_id NOT IN (
		SELECT UNNEST($2::UUID[])
	);`

	insertAuthorsQuery := /* sql */ `
	INSERT INTO author_book (author_id, book_id)
	SELECT author_id, $1
	FROM UNNEST($2::UUID[]) AS author_id
	ON CONFLICT (author_id, book_id) DO NOTHING;`

	if _, err = tx.Exec(ctx, clearAuthorsQuery, bookID, authorIDs); err != nil {
		return err
	}

	if _, err = tx.Exec(ctx, insertAuthorsQuery, bookID, authorIDs); err != nil {
		return err
	}

	if _, err = tx.Exec(ctx, updateBookQuery, name, time.Now(), bookID); err != nil {
		return err
	}

	span.SetAttributes(attribute.String("book.id", bookID))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "update book",
		"book_id":   bookID,
		"trace_id":  span.SpanContext().TraceID().String(),
	})

	return nil
}

func (p *postgresRepository) GetAuthorBooks(ctx context.Context, authorID string) (books []entity.Book, txErr error) {
	span := trace.SpanFromContext(ctx)
	defer span.End()

	start := time.Now()

	defer func() {
		GetAuthorBooksLatency.Observe(time.Since(start).Seconds())
	}()

	var (
		tx  pgx.Tx
		err error
	)

	if tx, err = extractTx(ctx); err != nil {
		tx, err = p.db.Begin(ctx)

		if err != nil {
			return nil, err
		}

		defer func(tx pgx.Tx, ctx context.Context) {
			if txErr != nil {
				err = tx.Rollback(ctx)
				if err != nil {
					logrus.Debug("failed to rollback transaction")
				}
				return
			}

			err = tx.Commit(ctx)
			if err != nil {
				logrus.Debug("failed to commit transaction")
			}
		}(tx, ctx)
	}

	if err != nil {
		return nil, err
	}

	const booksQuery = /* sql */ `
		SELECT
		book.id AS book_id,
		book.name AS book_name,
		book.created_at AS book_created_at,
		book.updated_at AS book_updated_at,
		author.id AS author_id
	FROM author_book
	INNER JOIN book ON author_book.book_id = book.id
	INNER JOIN author_book all_ab ON book.id = all_ab.book_id
	INNER JOIN author ON all_ab.author_id = author.id
	WHERE author_book.author_id = $1;
	`

	var res = make([]entity.Book, 0)
	rows, err := tx.Query(ctx, booksQuery, authorID)
	if err != nil {
		return nil, err
	}

	type entry struct {
		BookID    string
		BookName  string
		CreatedAt time.Time
		UpdatedAt time.Time
		AuthorID  string
	}

	entries, err := pgx.CollectRows(rows, func(row pgx.CollectableRow) (entry, error) {
		var n entry
		err = row.Scan(&n.BookID, &n.BookName, &n.CreatedAt, &n.UpdatedAt, &n.AuthorID)
		return n, err
	})
	if err != nil {
		return nil, err
	}

	var idMap map[string]entity.Book = make(map[string]entity.Book)

	for _, entry := range entries {
		if _, ok := idMap[entry.BookID]; !ok {
			var book = entity.Book{
				ID:        entry.BookID,
				Name:      entry.BookName,
				AuthorIDs: make([]string, 0),
				CreatedAt: entry.CreatedAt,
				UpdatedAt: entry.UpdatedAt,
			}

			idMap[entry.BookID] = book
		}

		book := idMap[entry.BookID]
		//nolint:gocritic // cannot assign to struct field idMap[entry.BookID].AuthorIDs in map
		book.AuthorIDs = append(idMap[entry.BookID].AuthorIDs, entry.AuthorID)

		idMap[entry.BookID] = book
	}

	for _, v := range idMap {
		res = append(res, v)
	}

	span.SetAttributes(attribute.String("author.id", authorID))
	logrus.WithFields(logrus.Fields{
		"component": "repository",
		"operation": "collect author books",
		"author_id": authorID,
		"trace_id":  span.SpanContext().TraceID().String(),
	})
	return res, nil
}

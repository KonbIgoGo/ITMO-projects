-- +goose Up
CREATE TABLE author_book
(
    author_id UUID NOT NULL,
    book_id   UUID NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    PRIMARY KEY (author_id, book_id)
);

-- +goose Down
DROP TABLE author_book;
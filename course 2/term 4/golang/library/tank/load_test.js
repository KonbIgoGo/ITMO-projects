import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween, randomItem } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    vus: 10,
    duration: '1m',
};

let authorIds = [];
let bookIds = [];

export default function () {
    const bookUrl = 'http://localhost:8080/v1/library/book';
    const bookInfoUrl = 'http://localhost:8080/v1/library/book_info';
    const authorUrl = 'http://localhost:8080/v1/library/author';
    const authorBooksUrl = 'http://localhost:8080/v1/library/author_books';

    const authorName = `Author${randomIntBetween(1000, 9999)}`;
    const authorPayload = JSON.stringify({ name: authorName });
    
    const authorRes = http.post(authorUrl, authorPayload, {
        headers: { 'Content-Type': 'application/json' }
    });

    let newAuthorId;
    if (check(authorRes, {
        'author created': (r) => r.status === 200 || r.status === 201,
    })) {
        try {
            const response = authorRes.json();
            if (response && response.id) {
                newAuthorId = response.id;
                authorIds.push(newAuthorId);
            } else {
                console.error('Author response missing ID:', authorRes.body);
            }
        } catch (e) {
            console.error('Author JSON parse error:', e.message);
        }
    }

    const bookName = `Book${randomIntBetween(1000, 9999)}`;
    const payload = JSON.stringify({
        name: bookName,
        author_ids: [newAuthorId],
    });

    const bookRes = http.post(bookUrl, payload, {
        headers: { 'Content-Type': 'application/json' }
    });

    let newBookId;
    if (check(bookRes, {
        'book created': (r) => r.status === 200 || r.status === 201,
    })) {
        try {
            const response = bookRes.json();
            if (response && response.book && response.book.id) {
                newBookId = response.book.id;
                bookIds.push(newBookId);
            } else {
                console.error('Book response missing ID:', bookRes.body);
            }
        } catch (e) {
            console.error('Book JSON parse error:', e.message);
        }
    }

    const getBookRes = http.get(`${bookInfoUrl}/${newBookId}`);
    const getAuthorRes = http.get(`${authorUrl}/${newAuthorId}`);
    const getAuthorBooksRes = http.get(`${authorBooksUrl}/${newAuthorId}`);

    check(getBookRes, {
        'book received (200)': (r) => r.status === 200,
    });
    check(getAuthorRes, {
        'author received (200)': (r) => r.status === 200,
    });

    check(getAuthorBooksRes, {
        'author books received (200)': (r) => r.status === 200,
    });




    const updateBookPayload = {
        id: randomItem(bookIds),
        name: "nameBook",
        authorIds: [randomItem(authorIds)]
    }

    const updateAuthorPayload = {
        id: randomItem(authorIds),
        name: "nameAuthor",
    }

    const updateBookRes = http.put(bookUrl, updateBookPayload)
    const updateAuthorRes = http.put(authorUrl, updateAuthorPayload)
    
    check(updateBookRes, {
        'book updated (200)': (r) => r.status === 200,
    });
    check(updateAuthorRes, {
        'author updated (200)': (r) => r.status === 200,
    });

    sleep(1);
}
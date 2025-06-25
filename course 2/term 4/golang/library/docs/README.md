# Library API

[In-memory](https://habr.com/ru/companies/headzio/articles/505792/) реализация сервиса библиотеки.

## Особенности
- **gRPC и Gateway**: поддержка взаимодействия через gRPС
- **Валидация входных данных**
- **Protobuf**: эффективный механизм сериализации данных

## API

### gRPC методы и REST пути
- **AddBook**
    - **gRPC**: `rpc AddBook(AddBookRequest) returns (AddBookResponse)`
    - **REST**: POST `v1/library/book`
- **GetBookInfo**
    - **gRPC**: `rpc GetBookInfo(GetBookInfoRequest) returns (GetBookInfoResponse)`
    - **REST**: GET `/v1/library/book_info/{id=*}`
- **UpdateBook**
    - **gRPC**: `rpc RegisterAuthor(RegisterAuthorRequest) returns (RegisterAuthorResponse)`
    - **REST**: PUT `v1/library/book`
- **RegisterAuthor**
    - **gRPC**: `rpc GetAuthorInfo(GetAuthorInfoRequest) returns (GetAuthorInfoResponse)`
    - **REST**: POST `/v1/library/author`
- **GetAuthorInfo**
    - **gRPC**: `rpc ChangeAuthorInfo(ChangeAuthorInfoRequest) returns (ChangeAuthorInfoResponse)`
    - **REST**: GET `/v1/library/author/{id=*}`
- **ChangeAuthorInfo**
    - **gRPC**: `rpc AddBook(AddBookRequest) returns (AddBookResponse)`
    - **REST**: PUT `/v1/library/author`
- **GetAuthorBooks**
    - **gRPC**: `rpc GetAuthorBooks(GetAuthorBooksRequest) returns (stream Book)`
    - **REST**: GET `/v1/library/author_books/{author_id}`

## Валидация входных данных

- **Идентификаторы**: все идентификаторы должны быть в формате [UUID](https://ru.wikipedia.org/wiki/UUID)
- **Формат имени автора**: Имя автора должно содержать хотя бы 1 символ и не превышать 512 символов

## Порты

Для корректной работы сервиса необходимо настроить следующие переменные окружения:
- `GRPC_PORT` - порт, на котором запускается gRPC сервер
- `GRPC_GATEWAY_PORT` - порт, на котором запускается REST to gRPC API

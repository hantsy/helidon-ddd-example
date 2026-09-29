-- Identifier columns use the native PostgreSQL `UUID` type. The application
-- normally supplies the id (see BookId/CopyId/LoanId value objects), while
-- uuid_generate_v4() (from the uuid-ossp extension) provides a v4 UUID default
-- when the application omits one.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS book
(
    id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    isbn  VARCHAR(32)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS copy
(
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id   UUID        NOT NULL,
    barcode   VARCHAR(64) NOT NULL,
    available BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS loan
(
    loan_id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    copy_id              UUID        NOT NULL,
    user_id              UUID        NOT NULL,
    created_at           TIMESTAMP   NOT NULL,
    expected_return_date DATE        NOT NULL,
    returned_at          TIMESTAMP,
    overdue_fee          NUMERIC(10, 2)
);

-- Identifier columns use the native PostgreSQL `UUID` type. The application
-- normally supplies the id (see BookId/CopyId/LoanId value objects), while
-- `gen_random_uuid()` (built into PostgreSQL 13+, no extension required) provides
-- a v4 UUID default when the application omits one.

CREATE TABLE IF NOT EXISTS book
(
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    isbn  VARCHAR(32)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS copy
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id   UUID        NOT NULL,
    barcode   VARCHAR(64) NOT NULL,
    available BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS loan
(
    loan_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    copy_id              UUID        NOT NULL,
    user_id              UUID        NOT NULL,
    created_at           TIMESTAMP   NOT NULL,
    expected_return_date DATE        NOT NULL,
    returned_at          TIMESTAMP,
    overdue_fee          NUMERIC(10, 2)
);

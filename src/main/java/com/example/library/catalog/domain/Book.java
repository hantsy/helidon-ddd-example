package com.example.library.catalog.domain;

import java.util.Objects;

/**
 * A book in the catalog. The ISBN is the natural business key; the {@link BookId}
 * is a surrogate identity assigned on creation.
 */
public class Book {
    private final BookId id;
    private final String title;
    private final Isbn isbn;

    public Book(String title, Isbn isbn) {
        this(new BookId(), title, isbn);
    }

    public Book(BookId id, String title, Isbn isbn) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.isbn = Objects.requireNonNull(isbn, "isbn must not be null");
    }

    public BookId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Isbn getIsbn() {
        return isbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

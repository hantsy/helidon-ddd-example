package com.example.library.catalog.domain;

import java.util.Objects;

/**
 * A physical copy of a {@link Book}, tracked for availability by the lending context.
 */
public class Copy {
    private final CopyId id;
    private final BookId bookId;
    private final BarCode barCode;
    private boolean available;

    public Copy(BookId bookId, BarCode barCode) {
        this(new CopyId(), bookId, barCode);
    }

    public Copy(CopyId id, BookId bookId, BarCode barCode) {
        this(id, bookId, barCode, true);
    }

    public Copy(CopyId id, BookId bookId, BarCode barCode, boolean available) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.bookId = Objects.requireNonNull(bookId, "bookId must not be null");
        this.barCode = Objects.requireNonNull(barCode, "barCode must not be null");
        this.available = available;
    }

    public CopyId id() {
        return id;
    }

    public BookId bookId() {
        return bookId;
    }

    public BarCode barCode() {
        return barCode;
    }

    public boolean isAvailable() {
        return available;
    }

    public void makeUnavailable() {
        this.available = false;
    }

    public void makeAvailable() {
        this.available = true;
    }
}

package com.example.library.catalog.domain;

import io.helidon.data.jdbc.JdbcClient;
import io.helidon.service.registry.Service;

import java.util.UUID;

/**
 * Maps a {@code book} row to a {@link Book} aggregate.
 */
@Service.Singleton
public final class BookRowMapper implements JdbcClient.RowMapper<Book> {
    @Override
    public Book map(JdbcClient.Row row) {
        return new Book(
                new BookId(UUID.fromString(row.get("id", String.class))),
                row.get("title", String.class),
                new Isbn(row.get("isbn", String.class)));
    }
}

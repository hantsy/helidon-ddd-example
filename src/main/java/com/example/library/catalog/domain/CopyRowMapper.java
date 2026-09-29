package com.example.library.catalog.domain;

import io.helidon.data.jdbc.JdbcClient;
import io.helidon.service.registry.Service;

import java.util.UUID;

/**
 * Maps a {@code copy} row to a {@link Copy} aggregate.
 */
@Service.Singleton
public final class CopyRowMapper implements JdbcClient.RowMapper<Copy> {
    @Override
    public Copy map(JdbcClient.Row row) {
        return new Copy(
                new CopyId(UUID.fromString(row.get("id", String.class))),
                new BookId(UUID.fromString(row.get("book_id", String.class))),
                new BarCode(row.get("barcode", String.class)),
                row.get("available", Boolean.class));
    }
}

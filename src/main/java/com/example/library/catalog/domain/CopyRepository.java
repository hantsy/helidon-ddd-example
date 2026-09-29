package com.example.library.catalog.domain;

import io.helidon.data.Data;
import io.helidon.data.jdbc.Jdbc;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for {@link Copy}. The implementation is generated at compile
 * time by Helidon Data JDBC from the {@code @Jdbc.Statement} methods below.
 * <p>
 * Identifiers are bound as {@link String} (the canonical {@link java.util.UUID}
 * form) with an explicit {@code ?::uuid} cast, because Helidon Data JDBC's
 * declarative SQL supports only portable scalar types, not {@code java.util.UUID}.
 */
@Data.Repository
@Data.Provider("jdbc")
public interface CopyRepository {

    @Jdbc.Statement("INSERT INTO copy (id, book_id, barcode, available) VALUES (?::uuid, ?::uuid, ?, ?)")
    void insert(String id, String bookId, String barcode, boolean available);

    @Jdbc.Statement("SELECT id, book_id, barcode, available FROM copy WHERE id = ?::uuid")
    @Jdbc.RowMapper(CopyRowMapper.class)
    Optional<Copy> findById(String id);

    @Jdbc.Statement("SELECT id, book_id, barcode, available FROM copy ORDER BY barcode")
    @Jdbc.RowMapper(CopyRowMapper.class)
    List<Copy> findAll();

    @Jdbc.Statement("UPDATE copy SET available = ? WHERE id = ?::uuid")
    @Jdbc.Execution(Jdbc.ExecutionType.UPDATE)
    long updateAvailability(boolean available, String id);

    @Jdbc.Statement("DELETE FROM copy")
    @Jdbc.Execution(Jdbc.ExecutionType.UPDATE)
    long deleteAll();
}

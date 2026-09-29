package com.example.library.catalog.domain;

import io.helidon.data.Data;
import io.helidon.data.jdbc.Jdbc;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for {@link Book}. The implementation is generated at compile
 * time by Helidon Data JDBC from the {@code @Jdbc.Statement} methods below.
 * <p>
 * Identifiers are bound as {@link String} (the canonical {@link java.util.UUID}
 * form) with an explicit {@code ?::uuid} cast, because Helidon Data JDBC's
 * declarative SQL supports only portable scalar types (String, numeric, temporal),
 * not {@code java.util.UUID}.
 */
@Data.Repository
@Data.Provider("jdbc")
public interface BookRepository {

    @Jdbc.Statement("INSERT INTO book (id, title, isbn) VALUES (?::uuid, ?, ?)")
    void insert(String id, String title, String isbn);

    @Jdbc.Statement("SELECT id, title, isbn FROM book WHERE id = ?::uuid")
    @Jdbc.RowMapper(BookRowMapper.class)
    Optional<Book> findById(String id);

    @Jdbc.Statement("SELECT id, title, isbn FROM book ORDER BY title")
    @Jdbc.RowMapper(BookRowMapper.class)
    List<Book> findAll();

    @Jdbc.Statement("SELECT count(*) > 0 FROM book WHERE isbn = ?")
    boolean existsByIsbn(String isbn);

    @Jdbc.Statement("DELETE FROM book")
    @Jdbc.Execution(Jdbc.ExecutionType.UPDATE)
    long deleteAll();
}

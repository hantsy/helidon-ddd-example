package com.example.library.lending.domain;

import io.helidon.data.jdbc.JdbcClient;
import io.helidon.service.registry.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps a {@code loan} row to a {@link Loan} aggregate.
 */
@Service.Singleton
public final class LoanRowMapper implements JdbcClient.RowMapper<Loan> {
    @Override
    public Loan map(JdbcClient.Row row) {
        return new Loan(
                new LoanId(UUID.fromString(row.get("loan_id", String.class))),
                new CopyId(UUID.fromString(row.get("copy_id", String.class))),
                new UserId(UUID.fromString(row.get("user_id", String.class))),
                row.get("created_at", LocalDateTime.class),
                row.get("expected_return_date", LocalDate.class),
                row.optional("returned_at", LocalDateTime.class).orElse(null),
                row.optional("overdue_fee", BigDecimal.class).orElse(null));
    }
}

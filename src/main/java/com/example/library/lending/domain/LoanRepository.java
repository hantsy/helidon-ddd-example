package com.example.library.lending.domain;

import io.helidon.data.Data;
import io.helidon.data.jdbc.Jdbc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for {@link Loan}. The implementation is generated at compile
 * time by Helidon Data JDBC from the {@code @Jdbc.Statement} methods below.
 * <p>
 * Identifiers are bound as {@link String} (the canonical {@link UUID} form) with an
 * explicit {@code ?::uuid} cast, because Helidon Data JDBC's declarative SQL supports
 * only portable scalar types, not {@code java.util.UUID}.
 */
@Data.Repository
@Data.Provider("jdbc")
public interface LoanRepository {

    @Jdbc.Statement("""
            INSERT INTO loan (loan_id, copy_id, user_id, created_at, expected_return_date, returned_at, overdue_fee)
            VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?)
            """)
    void insert(String loanId, String copyId, String userId, LocalDateTime createdAt,
                LocalDate expectedReturnDate, LocalDateTime returnedAt, BigDecimal overdueFee);

    @Jdbc.Statement("""
            SELECT loan_id, copy_id, user_id, created_at, expected_return_date, returned_at, overdue_fee
            FROM loan WHERE loan_id = ?::uuid
            """)
    @Jdbc.RowMapper(LoanRowMapper.class)
    Optional<Loan> findById(String loanId);

    @Jdbc.Statement("""
            SELECT loan_id, copy_id, user_id, created_at, expected_return_date, returned_at, overdue_fee
            FROM loan
            """)
    @Jdbc.RowMapper(LoanRowMapper.class)
    List<Loan> findAll();

    @Jdbc.Statement("SELECT count(*) = 0 FROM loan WHERE copy_id = ?::uuid AND returned_at IS NULL")
    boolean isAvailable(String copyId);

    @Jdbc.Statement("UPDATE loan SET returned_at = ?, overdue_fee = ? WHERE loan_id = ?::uuid")
    @Jdbc.Execution(Jdbc.ExecutionType.UPDATE)
    long updateReturned(LocalDateTime returnedAt, BigDecimal overdueFee, String loanId);

    @Jdbc.Statement("DELETE FROM loan")
    @Jdbc.Execution(Jdbc.ExecutionType.UPDATE)
    long deleteAll();

    default Loan findByIdOrThrow(String loanId) {
        return findById(loanId).orElseThrow(() -> new LoanNotFoundException(new LoanId(UUID.fromString(loanId))));
    }
}

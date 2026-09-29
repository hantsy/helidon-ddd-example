package com.example.library.lending.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * A loan of a {@link CopyId} to a {@link UserId}. The aggregate root of the
 * lending bounded context; it computes the overdue fee on return.
 */
public class Loan {
    private final LoanId loanId;
    private final CopyId copyId;
    private final UserId userId;
    private final LocalDateTime createdAt;
    private final LocalDate expectedReturnDate;
    private LocalDateTime returnedAt;
    private BigDecimal overdueFee;

    public Loan(CopyId copyId, UserId userId) {
        this(copyId, userId, LocalDateTime.now(), LocalDate.now().plusDays(30));
    }

    public Loan(CopyId copyId, UserId userId, LocalDateTime createdAt, LocalDate expectedReturnDate) {
        this(new LoanId(), copyId, userId, createdAt, expectedReturnDate, null, null);
    }

    public Loan(LoanId loanId, CopyId copyId, UserId userId, LocalDateTime createdAt,
                LocalDate expectedReturnDate, LocalDateTime returnedAt, BigDecimal overdueFee) {
        this.loanId = Objects.requireNonNull(loanId, "loanId must not be null");
        this.copyId = Objects.requireNonNull(copyId, "copyId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expectedReturnDate = Objects.requireNonNull(expectedReturnDate, "expectedReturnDate must not be null");
        this.returnedAt = returnedAt;
        this.overdueFee = overdueFee;
    }

    public LoanId id() {
        return loanId;
    }

    public CopyId copyId() {
        return copyId;
    }

    public UserId userId() {
        return userId;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDate expectedReturnDate() {
        return expectedReturnDate;
    }

    public LocalDateTime returnedAt() {
        return returnedAt;
    }

    public BigDecimal overdueFee() {
        return overdueFee;
    }

    public void returned() {
        this.returnedAt = LocalDateTime.now();
        if (this.returnedAt.isAfter(expectedReturnDate.atStartOfDay())) {
            var daysOverdue = ChronoUnit.DAYS.between(expectedReturnDate, returnedAt.toLocalDate());
            var fee = OverdueFee.forDays(daysOverdue);
            this.overdueFee = fee != null ? fee.amount() : null;
            // In production, fire an OverdueFeeCalculated domain event here
        }
    }
}

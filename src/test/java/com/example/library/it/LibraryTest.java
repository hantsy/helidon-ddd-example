package com.example.library.it;

import com.example.library.catalog.domain.BarCode;
import com.example.library.catalog.domain.Book;
import com.example.library.catalog.domain.BookRepository;
import com.example.library.catalog.domain.Copy;
import com.example.library.catalog.domain.CopyId;
import com.example.library.catalog.domain.CopyRepository;
import com.example.library.catalog.domain.Isbn;
import com.example.library.lending.application.RentBookUseCase;
import com.example.library.lending.application.ReturnBookUseCase;
import com.example.library.lending.domain.Loan;
import com.example.library.lending.domain.LoanRepository;
import com.example.library.lending.domain.OverdueFee;
import com.example.library.lending.domain.UserId;
import io.helidon.service.registry.ServiceRegistryManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * End-to-end integration test of the library use cases, backed by a PostgreSQL
 * container and exercising the cross-context domain events (the catalog observes
 * lending's {@code LoanCreated}/{@code LoanClosed} to keep copy availability in
 * sync, synchronously within the same local transaction).
 */
class LibraryTest {

    private static PostgreSQLContainer<?> postgres;
    private static ServiceRegistryManager manager;
    private static BookRepository bookRepository;
    private static CopyRepository copyRepository;
    private static LoanRepository loanRepository;
    private static RentBookUseCase rentBookUseCase;
    private static ReturnBookUseCase returnBookUseCase;

    @BeforeAll
    static void setUp() {
        postgres = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("library")
                .withUsername("test")
                .withPassword("test")
                .withInitScript("schema.sql");
        postgres.start();

        System.setProperty("data.clients.jdbc.0.name", "@default");
        System.setProperty("data.clients.jdbc.0.connection.url", postgres.getJdbcUrl());
        System.setProperty("data.clients.jdbc.0.connection.username", postgres.getUsername());
        System.setProperty("data.clients.jdbc.0.connection.password", postgres.getPassword());
        System.setProperty("data.clients.jdbc.0.connection.jdbc-driver-class-name", postgres.getDriverClassName());

        manager = ServiceRegistryManager.start();
        bookRepository = manager.registry().get(BookRepository.class);
        copyRepository = manager.registry().get(CopyRepository.class);
        loanRepository = manager.registry().get(LoanRepository.class);
        rentBookUseCase = manager.registry().get(RentBookUseCase.class);
        returnBookUseCase = manager.registry().get(ReturnBookUseCase.class);
    }

    @AfterAll
    static void tearDown() {
        if (manager != null) {
            manager.shutdown();
        }
        if (postgres != null) {
            postgres.stop();
        }
    }

    @BeforeEach
    void cleanUp() {
        loanRepository.deleteAll();
        copyRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void testLibraryCrud() {
        CopyId copyId = new CopyId();

        // Add a new Book
        Book book = new Book("Effective Java", new Isbn("9780134685991"));
        bookRepository.insert(book.getId().id().toString(), book.getTitle(), book.getIsbn().value());

        // Add some copies of the book
        Copy copy1 = new Copy(copyId, book.getId(), new BarCode("BC001"));
        Copy copy2 = new Copy(book.getId(), new BarCode("BC002"));
        copyRepository.insert(copy1.id().id().toString(), copy1.bookId().id().toString(),
                copy1.barCode().code(), copy1.isAvailable());
        copyRepository.insert(copy2.id().id().toString(), copy2.bookId().id().toString(),
                copy2.barCode().code(), copy2.isAvailable());

        // verify all copies
        assertThat(copyRepository.findAll()).hasSize(2);

        UserId userId = new UserId();
        // Rent a book
        rentBookUseCase.execute(com.example.library.lending.domain.CopyId.of(copyId.id()), userId);

        // Verify that the book is NOT available (event handled synchronously)
        var copyOptional = copyRepository.findById(copyId.id().toString());
        assertThat(copyOptional).isPresent();
        assertThat(copyOptional.get().isAvailable()).isFalse();

        // rent again should throw exception (the use case manages its own transaction)
        assertThrows(Exception.class,
                () -> rentBookUseCase.execute(com.example.library.lending.domain.CopyId.of(copyId.id()), userId));

        // verify ONLY one loan record
        var allLoans = loanRepository.findAll();
        assertThat(allLoans).hasSize(1);

        // Retrieve Loan
        Loan loan = loanRepository.findByIdOrThrow(allLoans.getFirst().id().id().toString());
        assertThat(loan.copyId().id()).isEqualTo(copyId.id());

        // Return the book
        returnBookUseCase.execute(loan.id());

        // Verify that the book is now available (event handled synchronously)
        var returnedCopyOptional = copyRepository.findById(copyId.id().toString());
        assertThat(returnedCopyOptional).isPresent();
        assertThat(returnedCopyOptional.get().isAvailable()).isTrue();
    }

    @Test
    void testOverdueReturn() {
        CopyId copyId = new CopyId();
        Book book = new Book("Domain-Driven Design", new Isbn("9780321125217"));
        bookRepository.insert(book.getId().id().toString(), book.getTitle(), book.getIsbn().value());
        copyRepository.insert(copyId.id().toString(), book.getId().id().toString(), "BC003", true);

        UserId userId = new UserId();
        // Create a loan with an expected return date 35 days in the past
        var pastDate = LocalDate.now().minusDays(35);
        var loan = new Loan(
                com.example.library.lending.domain.CopyId.of(copyId.id()),
                userId,
                LocalDateTime.now().minusDays(35),
                pastDate);
        loanRepository.insert(loan.id().id().toString(), loan.copyId().id().toString(), loan.userId().id().toString(),
                loan.createdAt(), loan.expectedReturnDate(), null, null);

        // Return the book — should trigger overdue fee
        returnBookUseCase.execute(loan.id());

        var updatedLoan = loanRepository.findByIdOrThrow(loan.id().id().toString());
        assertThat(updatedLoan.returnedAt()).isNotNull();
        assertThat(updatedLoan.overdueFee()).isEqualTo(OverdueFee.BEYOND_A_MONTH.amount());
    }
}

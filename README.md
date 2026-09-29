# Helidon DDD Example

A **Library** example demonstrating modular DDD with **Helidon 27 (SE)**, **JDK 27**, **Maven**, and **Helidon Data JDBC** for persistence.

It is the Helidon port of the [Quarkus DDD example](https://github.com/hantsy/quarkus-ddd-example), reusing the same domain model, bounded contexts and domain events, while replacing the persistence and DI stack with Helidon 27's forward-looking "Tip" stack.

## Architecture

Two bounded contexts (`catalog`, `lending`) plus a shared kernel (`common`):

```
com.example.library
├── catalog
│   ├── domain        Book, Copy (aggregates), BookId/CopyId/Isbn/BarCode (VOs),
│   │                 BookRepository, CopyRepository (@Data.Repository), BookSearchService (port)
│   ├── application   AddBookToCatalogUseCase, RegisterBookCopyUseCase,
│   │                 LoanCreatedEventListener, LoanClosedEventListener
│   └── infrastructure OpenLibraryBookSearchService (Helidon WebClient HTTP/1.1)
├── lending
│   ├── domain        Loan (aggregate), LoanId/CopyId/UserId (VOs), OverdueFee,
│   │                 LoanCreated/LoanClosed (events), LoanRepository
│   └── application   RentBookUseCase, ReturnBookUseCase, CopyAvailabilityValidator
└── common            DomainException
```

- **Persistence** — Helidon Data JDBC repositories (`@Data.Repository` + `@Jdbc.Statement`), with implementations generated at compile time by `helidon-data-jdbc-codegen` and rows mapped to aggregates by `@Service.Singleton` row mappers.
- **Use cases** — Helidon Inject `@Service.Singleton` beans demarcated with `@Tx.Required` (Helidon's local-JDBC transaction, replacing `@Transactional`).
- **Cross-context events** — Helidon Inject's in-process event bus: `Event.Emitter<LoanCreated>`/`Event.Emitter<LoanClosed>` fired by lending, observed synchronously by `catalog`'s `LoanCreatedEventListener`/`LoanClosedEventListener` (`@Event.Observer`) to toggle `Copy.available`.
- **Open Library ISBN search** — a Helidon WebClient **HTTP/1.1** (`Http1Client`) adapter with redirect following.
- **Database** — PostgreSQL with native `UUID` primary/foreign keys (and a `uuid_generate_v4()` v4 default via the `uuid-ossp` extension), exercised in tests via Testcontainers (`schema.sql` applied with `withInitScript`).

### Porting notes / deviations

- **UUID identifiers** — the schema uses native PostgreSQL `UUID` columns, but Helidon Data JDBC's declarative SQL supports only portable scalar types (String, numeric, temporal, `byte[]`), *not* `java.util.UUID`, and offers no converter/type-mapper for custom types. The repositories therefore bind identifiers as `String` with an explicit `?::uuid` cast and reconstruct `UUID` in the row mappers. Tracked upstream as [helidon-io/helidon#12647](https://github.com/helidon-io/helidon/issues/12647).
- **One observer per event type** — the original CDI listener had two `@Observes` methods in one class, but Helidon's codegen emits a colliding `__Observer` class name when a single class declares multiple `@Event.Observer` methods, so the port splits them into `LoanCreatedEventListener` and `LoanClosedEventListener`. Tracked upstream as [helidon-io/helidon#12648](https://github.com/helidon-io/helidon/issues/12648).
- **Optimistic locking** — the Quarkus source used JPA `@Version`; that is JPA-specific infrastructure, so the plain-JDBC port drops it (and the `version` column). Helidon Data JDBC has no `@Version`/optimistic-locking support — tracked upstream as [helidon-io/helidon#12653](https://github.com/helidon-io/helidon/issues/12653).
- **Lifecycle callbacks** — Helidon Data JDBC has no entity lifecycle hooks (no Spring Data `@BeforeConvert`/`@AfterConvert`/`@BeforeSave`/`@AfterSave` or JPA `@PrePersist`/`@PostLoad` auditing callbacks), so such logic lives in the use cases / row mappers. Tracked upstream as [helidon-io/helidon#12654](https://github.com/helidon-io/helidon/issues/12654).

## Build

```bash
mvn clean package          # compile (runs Helidon codegen), run all tests except the live one
mvn test -DliveTests=true -Dtest=BookSearchServiceTest   # run the live Open Library test
```

Prerequisites:

- JDK 27
- Maven 3.9+
- Docker (the integration test `LibraryTest` starts a PostgreSQL container)

The live-network test `BookSearchServiceTest` is `@Tag("integration")` and excluded from the default build (it hits the real Open Library API).

## Related examples

- [quarkus-ddd-example](https://github.com/hantsy/quarkus-ddd-example) — the Quarkus port (Jakarta Data + Hibernate)
- [spring-ddd-example](https://github.com/hantsy/spring-ddd-example) — the Spring Boot port
- [jakartaee-ddd-example](https://github.com/hantsy/jakartaee-ddd-example) — the original Jakarta EE port

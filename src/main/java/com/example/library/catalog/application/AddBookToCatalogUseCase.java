package com.example.library.catalog.application;

import com.example.library.catalog.domain.Book;
import com.example.library.catalog.domain.BookAlreadyExistsException;
import com.example.library.catalog.domain.BookInformation;
import com.example.library.catalog.domain.BookRepository;
import com.example.library.catalog.domain.BookSearchService;
import com.example.library.catalog.domain.Isbn;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;

@Service.Singleton
@Tx.Required
public class AddBookToCatalogUseCase {
    private final BookSearchService bookSearchService;
    private final BookRepository bookRepository;

    @Service.Inject
    public AddBookToCatalogUseCase(BookSearchService bookSearchService, BookRepository bookRepository) {
        this.bookSearchService = bookSearchService;
        this.bookRepository = bookRepository;
    }

    public void execute(Isbn isbn) {
        if (bookRepository.existsByIsbn(isbn.value())) {
            throw new BookAlreadyExistsException(isbn);
        }
        BookInformation result = bookSearchService.search(isbn);
        Book book = new Book(result.title(), isbn);
        bookRepository.insert(book.getId().id().toString(), book.getTitle(), book.getIsbn().value());
    }
}

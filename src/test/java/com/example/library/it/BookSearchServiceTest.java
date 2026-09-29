package com.example.library.it;

import com.example.library.catalog.domain.BookInformation;
import com.example.library.catalog.domain.BookNotFoundException;
import com.example.library.catalog.domain.BookSearchService;
import com.example.library.catalog.domain.Isbn;
import com.example.library.catalog.infrastructure.OpenLibraryBookSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for the {@link BookSearchService} domain port against the real
 * Open Library API (network dependent). Disabled by default; run with
 * {@code mvn test -DliveTests=true -Dtest=BookSearchServiceTest}.
 */
@EnabledIfSystemProperty(named = "liveTests", matches = "true")
class BookSearchServiceTest {

    private final BookSearchService bookSearchService =
            new OpenLibraryBookSearchService("https://openlibrary.org/");

    @Test
    void searchEffectiveJavaIsbnReturnsBookInformation() {
        BookInformation result = bookSearchService.search(new Isbn("9780134685991"));
        assertThat(result.title()).isEqualTo("Effective Java");
    }

    @Test
    void searchUnknownIsbnThrowsBookNotFoundException() {
        // 978-0-99999999-8 is a checksum-valid ISBN in an unallocated range:
        // Open Library returns 404, which the adapter maps to BookNotFoundException.
        assertThatThrownBy(() -> bookSearchService.search(new Isbn("9780999999998")))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("9780999999998");
    }
}

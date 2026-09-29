package com.example.library.catalog.infrastructure;

import com.example.library.catalog.domain.BookInformation;
import com.example.library.catalog.domain.BookNotFoundException;
import com.example.library.catalog.domain.BookSearchException;
import com.example.library.catalog.domain.Isbn;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link OpenLibraryBookSearchService}, the infrastructure adapter
 * behind the {@link com.example.library.catalog.domain.BookSearchService} domain port.
 * <p>
 * The Open Library endpoint is stubbed with okhttp {@code MockWebServer} on a random
 * port, so the adapter exercises the real Helidon WebClient stack against stubbed
 * success and failure responses, with no external network access.
 */
class OpenLibraryBookSearchServiceTest {

    /** A valid ISBN-13 (checksum correct) that is not allocated to any book. */
    private static final String UNKNOWN_ISBN = "9780000000002";
    private static final String KNOWN_ISBN = "9780134685991";

    private MockWebServer server;
    private OpenLibraryBookSearchService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new OpenLibraryBookSearchService(server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void searchWithKnownIsbnShouldReturnBookInformation() {
        server.enqueue(new MockResponse()
                .setResponseCode(302)
                .addHeader("Location", "/books/OL31838212M.json"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "title": "Effective Java",
                          "publishers": ["Addison-Wesley"],
                          "isbn_13": ["9780134685991"],
                          "revisions": 1
                        }
                        """));

        BookInformation result = service.search(new Isbn(KNOWN_ISBN));

        assertThat(result.title()).isEqualTo("Effective Java");
    }

    @Test
    void searchWithUnknownIsbnShouldThrowBookNotFoundExceptionWhenUpstreamReturns404() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining(UNKNOWN_ISBN);
    }

    @Test
    void searchShouldThrowBookSearchExceptionWhenUpstreamReturnsErrorStatus() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookSearchException.class)
                .hasMessageContaining("500");
    }

    @Test
    void searchShouldThrowBookSearchExceptionWhenNetworkFails() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookSearchException.class)
                .hasMessageContaining(UNKNOWN_ISBN);
    }
}

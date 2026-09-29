package com.example.library.catalog.infrastructure;

import com.example.library.catalog.domain.BookInformation;
import com.example.library.catalog.domain.BookNotFoundException;
import com.example.library.catalog.domain.BookSearchException;
import com.example.library.catalog.domain.Isbn;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link OpenLibraryBookSearchService}, the infrastructure adapter
 * behind the {@link com.example.library.catalog.domain.BookSearchService} domain port.
 * <p>
 * The Open Library endpoint is stubbed with WireMock on a random port, so the
 * adapter exercises the real Helidon WebClient stack against stubbed success and
 * failure responses, with no external network access.
 */
class OpenLibraryBookSearchServiceTest {

    /** A valid ISBN-13 (checksum correct) that is not allocated to any book. */
    private static final String UNKNOWN_ISBN = "9780000000002";
    private static final String KNOWN_ISBN = "9780134685991";

    private static WireMockServer server;
    private static OpenLibraryBookSearchService service;

    @BeforeAll
    static void setUp() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
        service = new OpenLibraryBookSearchService("http://localhost:" + server.port() + "/");
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @BeforeEach
    void resetWireMock() {
        server.resetAll();
    }

    @Test
    void searchWithKnownIsbnShouldReturnBookInformation() {
        server.stubFor(get(urlEqualTo("/isbn/" + KNOWN_ISBN + ".json"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "/books/OL31838212M.json")));
        server.stubFor(get(urlEqualTo("/books/OL31838212M.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "title": "Effective Java",
                                  "publishers": ["Addison-Wesley"],
                                  "isbn_13": ["9780134685991"],
                                  "revisions": 1
                                }
                                """)));

        BookInformation result = service.search(new Isbn(KNOWN_ISBN));

        assertThat(result.title()).isEqualTo("Effective Java");
    }

    @Test
    void searchWithUnknownIsbnShouldThrowBookNotFoundExceptionWhenUpstreamReturns404() {
        server.stubFor(get(urlEqualTo("/isbn/" + UNKNOWN_ISBN + ".json"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining(UNKNOWN_ISBN);
    }

    @Test
    void searchShouldThrowBookSearchExceptionWhenUpstreamReturnsErrorStatus() {
        server.stubFor(get(urlEqualTo("/isbn/" + UNKNOWN_ISBN + ".json"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookSearchException.class)
                .hasMessageContaining("500");
    }

    @Test
    void searchShouldThrowBookSearchExceptionWhenNetworkFails() {
        server.stubFor(get(urlEqualTo("/isbn/" + UNKNOWN_ISBN + ".json"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> service.search(new Isbn(UNKNOWN_ISBN)))
                .isInstanceOf(BookSearchException.class)
                .hasMessageContaining(UNKNOWN_ISBN);
    }
}

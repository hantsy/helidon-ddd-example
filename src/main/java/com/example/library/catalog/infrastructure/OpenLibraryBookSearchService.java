package com.example.library.catalog.infrastructure;

import com.example.library.catalog.domain.BookInformation;
import com.example.library.catalog.domain.BookNotFoundException;
import com.example.library.catalog.domain.BookSearchException;
import com.example.library.catalog.domain.BookSearchService;
import com.example.library.catalog.domain.Isbn;
import io.helidon.config.Config;
import io.helidon.service.registry.Service;
import io.helidon.webclient.http2.Http2Client;
import jakarta.json.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Open Library adapter implementing the {@link BookSearchService} domain port,
 * backed by the Helidon WebClient (HTTP/2). The Open Library base URL is
 * configurable via the {@code openlibrary.base-url} property.
 */
@Service.Singleton
public class OpenLibraryBookSearchService implements BookSearchService {
    private static final Logger LOGGER = Logger.getLogger(OpenLibraryBookSearchService.class.getName());

    /** Maximum number of HTTP redirects to follow before giving up. */
    private static final int MAX_REDIRECTS = 5;

    private final Http2Client client;

    @Service.Inject
    public OpenLibraryBookSearchService(Config config) {
        this(config.get("openlibrary.base-url").asString().orElse("https://openlibrary.org/"));
    }

    /**
     * Creates the adapter against a specific base URL, primarily for tests.
     *
     * @param baseUrl the Open Library base URL, e.g. {@code https://openlibrary.org/}
     */
    public OpenLibraryBookSearchService(String baseUrl) {
        this.client = Http2Client.create(builder -> builder
                .baseUri(baseUrl)
                // HTTP/1.1 with upgrade to HTTP/2, so the client also works with
                // plain HTTP/1.1 servers (Open Library, WireMock).
                .protocolConfig(pc -> pc.priorKnowledge(false)));
    }

    @Override
    public BookInformation search(Isbn isbn) {
        try {
            return doSearch(isbn);
        } catch (BookNotFoundException | BookSearchException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "network error searching isbn {0}: {1}",
                    new Object[]{isbn.value(), e.getMessage()});
            throw new BookSearchException("failed to search book for isbn: " + isbn.value(), e);
        }
    }

    private BookInformation doSearch(Isbn isbn) {
        try (var response = client.get("isbn/" + isbn.value() + ".json")
                .followRedirects(true)
                .maxRedirects(MAX_REDIRECTS)
                .request()) {
            int status = response.status().code();
            if (status == 404) {
                throw new BookNotFoundException(isbn);
            }
            if (status != 200) {
                LOGGER.log(Level.WARNING, "OpenLibrary returned unexpected status {0} for isbn {1}",
                        new Object[]{status, isbn.value()});
                throw new BookSearchException("failed to search book, upstream returned status " + status);
            }
            JsonObject json = response.as(JsonObject.class);
            return new BookInformation(json.getString("title"));
        }
    }
}

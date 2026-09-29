package com.example.library.catalog.domain;

import io.helidon.service.registry.Service;

/**
 * Domain port for searching book information (e.g. by ISBN) against an external
 * catalog. Implemented by infrastructure adapters such as the Open Library adapter.
 */
@Service.Contract
public interface BookSearchService {
    BookInformation search(Isbn isbn);
}

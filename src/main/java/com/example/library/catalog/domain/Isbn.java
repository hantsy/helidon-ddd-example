package com.example.library.catalog.domain;

import org.apache.commons.validator.routines.ISBNValidator;

public record Isbn(String value) {
    private static final ISBNValidator VALIDATOR = ISBNValidator.getInstance();

    public Isbn {
        if (!VALIDATOR.isValid(value)) {
            throw new IllegalArgumentException("invalid isbn: " + value);
        }
    }
}

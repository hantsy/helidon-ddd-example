package com.example.library.lending.domain;

import java.util.Objects;
import java.util.UUID;

public record CopyId(UUID id) {

    public CopyId {
        Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Reconstructs a CopyId from a raw UUID obtained from the catalog
     * bounded context. The lending context is a consumer of copy identities
     * — it never generates its own, which is why the no-arg constructor
     * is intentionally disabled here.
     */
    public static CopyId of(UUID id) {
        return new CopyId(id);
    }
}

package com.example.library.catalog.application;

import com.example.library.catalog.domain.Copy;
import com.example.library.catalog.domain.CopyId;
import com.example.library.catalog.domain.CopyNotFoundException;
import com.example.library.catalog.domain.CopyRepository;
import com.example.library.lending.domain.LoanCreated;
import io.helidon.service.registry.Event;
import io.helidon.service.registry.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observes the {@link LoanCreated} lending event and marks the borrowed catalog
 * {@link Copy} unavailable, synchronously within the transaction that fired the event.
 */
@Service.Singleton
public class LoanCreatedEventListener {
    private static final Logger LOGGER = Logger.getLogger(LoanCreatedEventListener.class.getName());
    private final CopyRepository copyRepository;

    @Service.Inject
    public LoanCreatedEventListener(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @Event.Observer
    public void onLoanCreated(LoanCreated event) {
        LOGGER.log(Level.INFO, "handling LoanCreated:{0}", new Object[]{event});
        var copyId = new CopyId(event.copyId().id());
        Copy copy = copyRepository.findById(copyId.id().toString()).orElseThrow(() -> new CopyNotFoundException(copyId));
        copy.makeUnavailable();
        copyRepository.updateAvailability(copy.isAvailable(), copyId.id().toString());
    }
}

package com.example.library.catalog.application;

import com.example.library.catalog.domain.Copy;
import com.example.library.catalog.domain.CopyId;
import com.example.library.catalog.domain.CopyNotFoundException;
import com.example.library.catalog.domain.CopyRepository;
import com.example.library.lending.domain.LoanClosed;
import io.helidon.service.registry.Event;
import io.helidon.service.registry.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observes the {@link LoanClosed} lending event and marks the returned catalog
 * {@link Copy} available again, synchronously within the transaction that fired the event.
 */
@Service.Singleton
public class LoanClosedEventListener {
    private static final Logger LOGGER = Logger.getLogger(LoanClosedEventListener.class.getName());
    private final CopyRepository copyRepository;

    @Service.Inject
    public LoanClosedEventListener(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @Event.Observer
    public void onLoanClosed(LoanClosed event) {
        LOGGER.log(Level.INFO, "handling LoanClosed:{0}", new Object[]{event});
        var copyId = new CopyId(event.copyId().id());
        Copy copy = copyRepository.findById(copyId.id().toString()).orElseThrow(() -> new CopyNotFoundException(copyId));
        copy.makeAvailable();
        copyRepository.updateAvailability(copy.isAvailable(), copyId.id().toString());
    }
}

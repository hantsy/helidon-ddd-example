package com.example.library.lending.application;

import com.example.library.lending.domain.CopyId;
import com.example.library.lending.domain.CopyNotAvailableException;
import com.example.library.lending.domain.LoanRepository;
import io.helidon.service.registry.Service;

@Service.Singleton
public class CopyAvailabilityValidator {
    private final LoanRepository loanRepository;

    @Service.Inject
    public CopyAvailabilityValidator(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void checkAvailable(CopyId copyId) {
        if (!loanRepository.isAvailable(copyId.id().toString())) {
            throw new CopyNotAvailableException(copyId);
        }
    }
}

package com.example.library.lending.application;

import com.example.library.lending.domain.CopyId;
import com.example.library.lending.domain.Loan;
import com.example.library.lending.domain.LoanCreated;
import com.example.library.lending.domain.LoanRepository;
import com.example.library.lending.domain.UserId;
import io.helidon.service.registry.Event;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service.Singleton
@Tx.Required
public class RentBookUseCase {
    private static final Logger LOGGER = Logger.getLogger(RentBookUseCase.class.getName());
    private final LoanRepository loanRepository;
    private final CopyAvailabilityValidator copyAvailabilityValidator;
    private final Event.Emitter<LoanCreated> loanCreatedEvent;

    @Service.Inject
    public RentBookUseCase(LoanRepository loanRepository,
                           CopyAvailabilityValidator copyAvailabilityValidator,
                           Event.Emitter<LoanCreated> loanCreatedEvent) {
        this.loanRepository = loanRepository;
        this.copyAvailabilityValidator = copyAvailabilityValidator;
        this.loanCreatedEvent = loanCreatedEvent;
    }

    public void execute(CopyId copyId, UserId userId) {
        copyAvailabilityValidator.checkAvailable(copyId);
        var now = LocalDateTime.now();
        Loan loan = new Loan(copyId, userId, now, LocalDate.now().plusDays(30));
        loanRepository.insert(loan.id().id().toString(), loan.copyId().id().toString(), loan.userId().id().toString(),
                loan.createdAt(), loan.expectedReturnDate(), null, null);

        LOGGER.log(Level.INFO, "firing LoanCreated with copy id = " + copyId);
        loanCreatedEvent.emit(new LoanCreated(copyId));
    }
}

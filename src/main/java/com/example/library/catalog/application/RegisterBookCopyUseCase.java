package com.example.library.catalog.application;

import com.example.library.catalog.domain.BarCode;
import com.example.library.catalog.domain.BookId;
import com.example.library.catalog.domain.Copy;
import com.example.library.catalog.domain.CopyRepository;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;

@Service.Singleton
@Tx.Required
public class RegisterBookCopyUseCase {
    private final CopyRepository copyRepository;

    @Service.Inject
    public RegisterBookCopyUseCase(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    public void execute(BookId bookId, BarCode barCode) {
        Copy copy = new Copy(bookId, barCode);
        copyRepository.insert(copy.id().id().toString(), copy.bookId().id().toString(), copy.barCode().code(), copy.isAvailable());
    }
}

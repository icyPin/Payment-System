package service;

import com.example.Payment.dto.TransactionRequest;
import com.example.Payment.dto.TransactionResponse;
import jakarta.transaction.Transactional;
import com.example.Payment.model.TransactionRecord;
import com.example.Payment.model.TransactionType;
import com.example.Payment.model.Wallet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repo.TransactionRecordRepository;
import repo.WalletRepository;

@Service
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRecordRepository transactionRepository;

    public TransactionService(WalletRepository walletRepository, TransactionRecordRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        if (transactionRepository.existsById(request.transactionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate transaction.");
        }

        Wallet wallet = walletRepository.findByIdWithLock(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        if (request.type() == TransactionType.DEBIT) {
            if (wallet.getBalance().compareTo(request.amount()) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds.");
            }
            wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        } else {
            wallet.setBalance(wallet.getBalance().add(request.amount()));
        }

        try {
            transactionRepository.saveAndFlush(new TransactionRecord(
                    request.transactionId(), request.userId(), request.amount(), request.type()
            ));
            walletRepository.save(wallet);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate transaction.");
        }

        return new TransactionResponse(request.transactionId(), wallet.getBalance(), "SUCCESS");
    }
}

package com.example.Payment.service;

import com.example.Payment.dto.TransactionRequest;
import com.example.Payment.dto.TransactionResponse;
import org.springframework.transaction.annotation.Transactional;
import com.example.Payment.model.TransactionRecord;
import com.example.Payment.model.TransactionType;
import com.example.Payment.model.Wallet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.Payment.repo.TransactionRecordRepository;
import com.example.Payment.repo.WalletRepository;

import java.math.BigDecimal;

@Service
@Transactional
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRecordRepository transactionRepository;

    public TransactionService(WalletRepository walletRepository, TransactionRecordRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
        Wallet wallet = walletRepository.findByIdWithLock(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        if (transactionRepository.existsById(request.transactionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate transaction.");
        }

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
                    request.transactionId(),
                    request.userId(),
                    request.amount(),
                    request.type()
            ));
            walletRepository.save(wallet);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate transaction.");
        }

        return new TransactionResponse(request.transactionId(), wallet.getBalance(), "SUCCESS");
    }
}
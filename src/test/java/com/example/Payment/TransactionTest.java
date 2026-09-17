package com.example.Payment;

import dto.TransactionRequest;
import model.TransactionType;
import model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import repo.TransactionRecordRepository;
import repo.WalletRepository;
import service.TransactionService;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransactionTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRecordRepository transactionRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Processes a single valid debit transaction successfully.")
    void testHappyPath() {
        walletRepository.save(new Wallet(testUserId, new BigDecimal("500.00")));

        transactionService.processTransaction(new TransactionRequest(
                UUID.randomUUID(), testUserId, new BigDecimal("100.00"), TransactionType.DEBIT));

        Wallet updatedWallet = walletRepository.findById(testUserId).orElseThrow();
        assertEquals(0, new BigDecimal("400.00").compareTo(updatedWallet.getBalance()));
        System.out.println("Result: Processed single debit successfully.");
    }

    @Test
    @DisplayName("Sends 3 identical transactionIDs simultaneously. Ensures the balance is only deducted once.")
    void testIdempotency() {
        walletRepository.save(new Wallet(testUserId, new BigDecimal("1000.00")));
        UUID sharedTxId = UUID.randomUUID();
        AtomicInteger conflictCount = new AtomicInteger(0);
        IntStream.range(0, 3).parallel().forEach(i -> {
            try {
                transactionService.processTransaction(new TransactionRequest(
                        sharedTxId, testUserId, new BigDecimal("250.00"), TransactionType.DEBIT));
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().value() == 409) {
                    conflictCount.incrementAndGet();
                }
            }
        });

        Wallet wallet = walletRepository.findById(testUserId).orElseThrow();
        assertEquals(2, conflictCount.get());
        assertEquals(0, new BigDecimal("750.00").compareTo(wallet.getBalance()));
        System.out.println("Result: 1 Success, 2 Conflicts, Balance is 750.00.");
    }

    @Test
    @DisplayName("Sends 10 concurrent debit requests of 100 for a wallet with a 500 balance. Ensures the final balance is exactly 0 and 5 requests fail with insufficient funds.")
    void testRaceCondition() {
        walletRepository.save(new Wallet(testUserId, new BigDecimal("500.00")));
        AtomicInteger failureCount = new AtomicInteger(0);

        IntStream.range(0, 10).parallel().forEach(i -> {
            try {
                transactionService.processTransaction(new TransactionRequest(
                        UUID.randomUUID(), testUserId, new BigDecimal("100.00"), TransactionType.DEBIT));
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().value() == 422) {
                    failureCount.incrementAndGet();
                }
            }
        });

        Wallet wallet = walletRepository.findById(testUserId).orElseThrow();
        assertEquals(5, failureCount.get());
        assertEquals(0, new BigDecimal("0.00").compareTo(wallet.getBalance()));
        System.out.println("Result: 5 Success, 5 Failed, Balance is 0.00.");
    }
}
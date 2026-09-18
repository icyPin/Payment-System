package com.example.Payment.dto;

import com.example.Payment.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(
        UUID transactionId,
        UUID userId,
        BigDecimal amount,
        TransactionType type
) {}

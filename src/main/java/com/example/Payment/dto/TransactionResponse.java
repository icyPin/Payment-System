package com.example.Payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        BigDecimal currentBalance,
        String status
) {}
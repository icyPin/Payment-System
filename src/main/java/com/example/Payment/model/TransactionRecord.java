package com.example.Payment.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionRecord  {

    @Column(nullable = false)
    private UUID userId;

    @Id
    private UUID transactionId;

    @Column(nullable = false, precision = 18 , scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType tranType;

    public TransactionRecord(UUID transactionId, UUID userId, BigDecimal amount, TransactionType type) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.tranType = tranType;
    }

    public TransactionRecord(){}

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmmount() {
        return amount;
    }

    public void setAmmount(BigDecimal ammount) {
        this.amount = ammount;
    }

    public TransactionType getTranType() {
        return tranType;
    }

    public void setTranType(TransactionType tranType) {
        this.tranType = tranType;
    }
}

package model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
    private BigDecimal ammount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType tranType;

    public TransactionRecord(UUID userId, UUID transactionId, BigDecimal ammount, TransactionType tranType) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.ammount = ammount;
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
        return ammount;
    }

    public void setAmmount(BigDecimal ammount) {
        this.ammount = ammount;
    }

    public TransactionType getTranType() {
        return tranType;
    }

    public void setTranType(TransactionType tranType) {
        this.tranType = tranType;
    }
}

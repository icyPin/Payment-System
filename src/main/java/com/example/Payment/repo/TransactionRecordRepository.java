package com.example.Payment.repo;

import com.example.Payment.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, UUID> {
}

package com.example.ai_assistant_backend.repository;

import com.example.ai_assistant_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserId(UUID userId);

    List<Payment> findByUserIdAndStatus(UUID userId, String status);

    boolean existsByTransactionId(String transactionId);
}

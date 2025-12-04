package com.high.payment.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}

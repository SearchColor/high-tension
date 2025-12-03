package com.high.payment.infrastrucutre.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.PaymentOutbox;

public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutbox, UUID> {
}

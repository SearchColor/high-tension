package com.high.payment.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.PaymentOutbox;

public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutbox, UUID> {

	List<PaymentOutbox> findTop100ByOrderByCreatedAtAsc();

	List<PaymentOutbox> findAllByStatus(PaymentOutbox.OutboxStatus status, Pageable pageable);
}

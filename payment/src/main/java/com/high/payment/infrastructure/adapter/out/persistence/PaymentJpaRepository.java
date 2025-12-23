package com.high.payment.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentStatus;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

	Optional<Payment> findByOrderId(UUID orderID);

	// limit 처리: Pageable 사용
	List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime before, Pageable pageable);
}

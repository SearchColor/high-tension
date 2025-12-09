package com.high.payment.infrastrcutre.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.payment.domain.model.Payment;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

	Optional<Payment> findByOrderId(UUID orderID);
}

package com.high.product.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.ProcessedSaga;

public interface JpaProcessedSagaRepository extends JpaRepository<ProcessedSaga, UUID> {
}
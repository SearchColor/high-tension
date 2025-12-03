package com.high.orchestration.infrastructure.repository;

import com.high.orchestration.domain.entity.SagaState;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaStateRepository extends JpaRepository<SagaState, UUID> {

}

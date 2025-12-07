package com.high.external.infrastructure.persistence;

import com.high.external.domain.model.SlackRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlackRecordJpaRepository extends JpaRepository<SlackRecord, UUID> {
}

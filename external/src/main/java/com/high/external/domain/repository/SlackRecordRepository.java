package com.high.external.domain.repository;

import com.high.external.domain.model.SlackRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SlackRecordRepository {

    void save(SlackRecord slackRecord);

    Optional<SlackRecord> findById(UUID slackRecordId);

    Page<SlackRecord> findAll(Pageable pageable);
}

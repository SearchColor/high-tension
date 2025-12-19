package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.domain.KafkaDlqMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KafkaDlqJpaRepository extends JpaRepository<KafkaDlqMessage, UUID> {

    @Query("""
        SELECT d FROM KafkaDlqMessage d
        WHERE d.originalTopic = :topic
        AND d.dlqStatus = 'RETRY_WAITING'
        AND d.nextRetryAt <= :now
        ORDER BY d.createdAt ASC
        """)
    List<KafkaDlqMessage> findRetryTargetsByTopic(
        @Param("topic") String topic,
        @Param("now") LocalDateTime now
    );
}

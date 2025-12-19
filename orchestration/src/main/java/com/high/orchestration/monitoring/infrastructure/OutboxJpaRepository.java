package com.high.orchestration.monitoring.infrastructure;

import com.high.orchestration.monitoring.domain.Outbox;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxJpaRepository extends JpaRepository<Outbox, UUID> {

    @Query("""
        SELECT o FROM Outbox o
        WHERE o.originalTopic = :topic
        AND o.dlqStatus = 'RETRY_WAITING'
        AND o.nextRetryAt <= :now
        ORDER BY o.createdAt ASC
        """)
    List<Outbox> findRetryTargetsByTopic(
        @Param("topic") String topic,
        @Param("now") LocalDateTime now
    );
}

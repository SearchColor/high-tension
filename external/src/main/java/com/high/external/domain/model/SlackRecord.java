package com.high.external.domain.model;


import com.library.jpa.common.entity.BaseDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "p_slack_records")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class SlackRecord extends BaseDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String recipientId;

    @Column(nullable = false)
    private String message;


    public static SlackRecord create(String recipientId, String message){
        return SlackRecord.builder()
                .recipientId(recipientId)
                .message(message)
                .build();
    }

}

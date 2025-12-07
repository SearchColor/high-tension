package com.high.external.application.dto.response;

import com.high.external.domain.model.SlackRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SlackRecordDto {

    private final UUID slackRecordId;
    private final String recipientId;
    private final String message;
    private final LocalDateTime createdAt;
    private final UUID createdBy;

    public static SlackRecordDto from(
            SlackRecord slackRecord
    ){
        return SlackRecordDto.builder()
                .slackRecordId(slackRecord.getId())
                .recipientId(slackRecord.getRecipientId())
                .message(slackRecord.getMessage())
                .createdAt(slackRecord.getCreatedAt())
                .createdBy(slackRecord.getCreatedBy())
                .build();
    }
}

package com.high.orchestration.monitoring.application;

import com.high.orchestration.monitoring.application.dto.DlqPermanentFailedMessage;

public interface DlqEventPublisher {
    void publishPermanentFailed(String topic, DlqPermanentFailedMessage message);

}

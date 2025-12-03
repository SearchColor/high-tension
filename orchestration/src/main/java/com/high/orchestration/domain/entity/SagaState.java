package com.high.orchestration.domain.entity;

import com.high.orchestration.domain.vo.CurrentStep;
import com.high.orchestration.domain.vo.SagaStatus;
import com.high.orchestration.domain.vo.SagaType;
import com.library.jpa.common.entity.BaseUpdateEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_saga_state")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SagaState extends BaseUpdateEntity {

    @Id
    private UUID sagaId;

    private UUID orderId;

    private SagaType sagaType;

    private SagaStatus sagaStatus;

    private CurrentStep currentStep;

    private String payload;

    private String errorMessage;

    private SagaState(UUID sagaId, UUID orderId, SagaType sagaType, SagaStatus sagaStatus, CurrentStep currentStep, String payload, String errorMessage) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.sagaType = sagaType;
        this.sagaStatus = sagaStatus;
        this.currentStep = currentStep;
        this.payload = payload;
        this.errorMessage = errorMessage;
    }

    public static SagaState create(
        UUID sagaId,
        UUID orderId,
        SagaType sagaType,
        CurrentStep currentStep,
        String payload,
        String errorMessage
    ) {
        return new SagaState(
            sagaId,
            orderId,
            sagaType,
            SagaStatus.STARTED,
            currentStep,
            payload,
            errorMessage
        );
    }


}

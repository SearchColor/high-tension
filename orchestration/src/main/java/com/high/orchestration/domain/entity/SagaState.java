package com.high.orchestration.domain.entity;

import com.high.orchestration.domain.vo.CurrentStep;
import com.high.orchestration.domain.vo.SagaStatus;
import com.high.orchestration.domain.vo.SagaType;
import com.library.jpa.common.entity.BaseUpdateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

    @Enumerated(EnumType.STRING)
    private SagaType sagaType;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    @Enumerated(EnumType.STRING)
    private CurrentStep currentStep;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String errorMessage;

    @Version
    private Long version;


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

    public void updateSagaState(
        SagaStatus sagaStatus,
        CurrentStep currentStep,
        String payload
    ) {
        this.sagaStatus = sagaStatus == null ? this.sagaStatus : sagaStatus;
        this.currentStep = currentStep == null ? this.currentStep : currentStep;
        this.payload = payload == null ? this.payload : payload;

    }

    public void updateCurrentStep(CurrentStep currentStep) {
        this.currentStep = currentStep;
    }

    public void recordError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.sagaStatus = SagaStatus.FAILED;
    }

    public void fail(String errorMessage) {
        this.sagaStatus = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void updateSagaStatus(SagaStatus sagaStatus) {
        this.sagaStatus = sagaStatus;
    }

    public void updateOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}

package com.high.coupon.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.coupon.application.CouponIssueService;
import com.high.coupon.infrastructure.kafka.consumer.CouponUseRequestConsumer;
import com.high.coupon.infrastructure.kafka.dto.CouponUseRequestMessage;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

/**
 * todo 임시 test: KafkaConsumer 작동 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class CouponUseRequestConsumerTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CouponIssueService couponIssueService;

    @InjectMocks
    private CouponUseRequestConsumer couponUseRequestConsumer;

    // === 공통 필드 (모든 테스트에서 재사용) ===
    UUID orderId;
    UUID sagaId;
    UUID userId;
    String jsonMessage;

    @BeforeEach
    void setUp() throws Exception {
        orderId = UUID.randomUUID();
        sagaId = UUID.randomUUID();
        userId = UUID.randomUUID();

        CouponUseRequestMessage request =
                new CouponUseRequestMessage(sagaId, orderId, userId);

        jsonMessage = objectMapper.writeValueAsString(request);
    }

    @Test
    @DisplayName("성공: 정상적인 메시지가 오면 서비스 로직을 수행하고 Ack 처리")
    void testConsumeCouponRequest_Success() {
        // given
        Acknowledgment ack = mock(Acknowledgment.class);

        // when
        couponUseRequestConsumer.consumeCouponRequest(jsonMessage, ack);

        // then
        verify(couponIssueService, times(1)).useCouponByOrderId(orderId);
        verify(ack, times(1)).acknowledge();
    }

    @Test
    @DisplayName("실패 1: 비즈니스 로직 에러 but Ack 수행 (로그만 남김)")
    void testConsumeCouponRequest_ServiceError() throws Exception {
        // given
        Acknowledgment ack = mock(Acknowledgment.class);

        // 서비스가 에러를 뱉도록 임시 설정
        doThrow(new RuntimeException("=== DB 연결 오류")).when(couponIssueService).useCouponByOrderId(any());

        // when
        couponUseRequestConsumer.consumeCouponRequest(jsonMessage, ack);

        // then
        verify(couponIssueService, times(1)).useCouponByOrderId(orderId);
        verify(ack, times(1)).acknowledge();
    }

    @Test
    @DisplayName("실패 2: JSON 형식이 파싱 오류 but Ack 수행")
    void testConsumeCouponRequest_JsonParseError() {
        // given
        String invalidJson = "{ \"weird_field\": \"value\" "; // 깨진 JSON 형식
        Acknowledgment ack = mock(Acknowledgment.class);

        // when
        couponUseRequestConsumer.consumeCouponRequest(invalidJson, ack);

        // then
        // 파싱 오류 - 서비스 로직 실행 X
        verify(couponIssueService, never()).useCouponByOrderId(any());

        // Ack 수행
        verify(ack, times(1)).acknowledge();
    }
}
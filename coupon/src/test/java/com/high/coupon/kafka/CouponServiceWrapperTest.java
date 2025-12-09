package com.high.coupon.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.repository.CouponIssueRepository;
import com.high.coupon.infrastructure.client.OrderClient;
import com.high.coupon.infrastructure.client.dto.OrderResponse;
import com.library.module.response.ApiResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * todo 새로 생성한 wrapper method 테스트용
 */
@ExtendWith(MockitoExtension.class)
public class CouponServiceWrapperTest {

    @Mock
    private OrderClient orderClient;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    @InjectMocks
    private CouponIssueService couponIssueService;

    @DisplayName("orderId 기반 쿠폰 사용 처리 성공")
    @Test
    void testUseCouponByOrderId() {

        UUID orderId = UUID.randomUUID();
        UUID couponIssueId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // OrderClient Res
        OrderResponse mockOrder = new OrderResponse(
                orderId,
                userId,
                couponIssueId
        );

        // 성공 가정
        when(orderClient.getOrder(orderId))
                .thenReturn(ApiResponse.success(mockOrder));

        // CouponIssue Mock 생성
        CouponIssue issue = mock(CouponIssue.class);

        // 내부 로직 Mocking
        doNothing().when(issue).useCoupon(any(), any());

        when(couponIssueRepository.findByIdAndUserId(couponIssueId, userId))
                .thenReturn(Optional.of(issue));

        // 실행
        couponIssueService.useCouponByOrderId(orderId);

        // 검증
        verify(issue).useCoupon(eq(userId), any());
    }
}

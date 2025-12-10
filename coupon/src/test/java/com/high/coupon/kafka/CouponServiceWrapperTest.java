package com.high.coupon.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.high.coupon.application.CouponIssueService;
import com.high.coupon.application.provider.OrderProvider;
import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.repository.CouponIssueRepository;
import com.high.coupon.infrastructure.client.dto.OrderResponse;
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
    private OrderProvider orderProvider;

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

        when(orderProvider.getOrder(orderId))
                .thenReturn(mockOrder);

        // CouponIssue Mock 생성 및 동작 정의
        CouponIssue issue = mock(CouponIssue.class);
        doNothing().when(issue).useCoupon(any(), any()); // 내부 로직 Mocking

        when(couponIssueRepository.findByIdAndUserId(couponIssueId, userId))
                .thenReturn(Optional.of(issue));

        // when
        couponIssueService.useCouponByOrderId(orderId);

        // then
        verify(orderProvider, times(1)).getOrder(orderId);
        verify(issue).useCoupon(eq(userId), any());
    }
}

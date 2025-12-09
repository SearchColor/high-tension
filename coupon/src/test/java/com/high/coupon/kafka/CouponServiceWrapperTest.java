package com.high.coupon.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        UUID orderId = UUID.randomUUID(); // kafka에게 받은 orderId
        UUID couponIssueId = UUID.randomUUID(); // order 정보
        UUID userId = UUID.randomUUID(); // order 정보

        // OrderClient Res
        OrderResponse mockOrder = new OrderResponse(
                orderId,
                userId,
                couponIssueId
        );

        // 성공 가정
        when(orderClient.getOrder(orderId))
                .thenReturn(ApiResponse.success(mockOrder));

        // CouponIssue
        CouponIssue issue = mock(CouponIssue.class);

        when(couponIssueRepository.findById(couponIssueId))
                .thenReturn(Optional.of(issue));

        // 실행 - orderId 기반으로 couponIssueId + userId를 조회해 내부 useCoupon() 호출까지
        couponIssueService.useCouponByOrderId(orderId);

        // 검증
        // → eq(userId): userId가 정확히 전달
        verify(issue).useCoupon(eq(userId), any());
    }
}

package com.high.coupon.application;

import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.high.coupon.application.dto.response.CouponUseResponse;
import com.high.coupon.application.dto.response.CouponValidationResponse;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.high.coupon.application.exception.CouponIssueNotFoundException;
import com.high.coupon.application.exception.CouponOutOfStockException;
import com.high.coupon.application.provider.OrderProvider;
import com.high.coupon.application.provider.dto.OrderInfo;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.exception.CouponAlreadyIssuedException;
import com.high.coupon.domain.exception.CouponNotOwnedException;
import com.high.coupon.domain.repository.CouponIssueRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponIssueService {

    // todo : 고도화 진행 중 (발급 파트)

    private final CouponService couponService;
    private final CouponIssueRepository couponIssueRepository;
    private final OrderProvider orderProvider;

    // todo 계층 관계 확인 필요
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> issueCouponScript;

    /**
     * 쿠폰 발급
     */
    @Transactional
    public CouponIssueResponse issueCoupon(UUID couponId, UUID userId) {

        log.info("User {} is issued a coupon", userId);

        Coupon coupon = couponService.getCouponById(couponId);

        // Redis Lua Script
        String key = "coupon:" + couponId + ":users";

        Long result = redisTemplate.execute(
                issueCouponScript,
                Collections.singletonList(key), // KEYS[1]
                userId.toString(),            // ARGV[1]
                String.valueOf(coupon.getTotalQuantity()) // ARGV[2]
        );

        // 0 성공, -1 실패: 수량 소진, -2 실패: 이미 발급 받음
        if (Long.valueOf(-2).equals(result)) {
            throw new CouponAlreadyIssuedException();
        }
        if (Long.valueOf(-1).equals(result)) {
            throw new CouponOutOfStockException();
        }

        // todo Lua에서 남길 코드들 - 동기, 비동기 전환 필요
        CouponIssue couponIssue = CouponIssue.issueCoupon(coupon, userId, LocalDateTime.now());
        CouponIssue savedCouponIssue = couponIssueRepository.save(couponIssue);

        return CouponIssueResponse.from(savedCouponIssue);
    }



    /**
     * ----- internal 메서드 -----
     */

    // 사용자 별 사용 가능한 쿠폰 조회
    public List<UserCouponResponse> getAvailableUserCoupons(UUID userId) {

        List<CouponIssue> issues =
                couponIssueRepository.findAvailableByUserId(userId);

        return issues.stream()
                .map(UserCouponResponse::from)
                .toList();
    }


    // 쿠폰 단건 유효성 검증용
    public CouponValidationResponse validateCoupon(UUID couponIssueId, UUID userId) {

        CouponIssue couponIssue = couponIssueRepository.findById(couponIssueId)
                .orElseThrow(CouponIssueNotFoundException::new);

        if (!couponIssue.getUserId().equals(userId)) {
            throw new CouponNotOwnedException();
        }

        couponIssue.validateUsable(userId, LocalDateTime.now());
        return CouponValidationResponse.from(couponIssue);
    }


    // 쿠폰 사용 처리
    @Transactional
    public CouponUseResponse useCoupon(UUID couponIssueId, UUID userId){

        CouponIssue couponIssue = couponIssueRepository.findByIdAndUserId(couponIssueId, userId)
                .orElseThrow(CouponNotOwnedException::new);

        couponIssue.useCoupon(userId, LocalDateTime.now());
        log.info("[INTERNAL] Coupon-Issue-Service - 쿠폰 사용처리 : couponIssueId={}, userId={}", couponIssueId, userId);
        return CouponUseResponse.from(couponIssue);
    }

    // 쿠폰 복원 처리 (주문/결제 취소)
    @Transactional
    public CouponUseResponse restoreCoupon(UUID couponIssueId) {

        CouponIssue couponIssue = couponIssueRepository.findById(couponIssueId)
                .orElseThrow(CouponIssueNotFoundException::new);

        couponIssue.restoreCoupon(LocalDateTime.now());
        log.info("[INTERNAL] Coupon-Issue-Service - 쿠폰 복원 프로세스 종료 : couponIssueId={}", couponIssueId);
        return CouponUseResponse.from(couponIssue);
    }



    /**
     * ========================================================================
     * >>>>>> saga / kafka 연계용 wrapper 메서드
     * orderId를 기반으로 couponIssuedId를 조회하고 기존 메서드는 재사용 하도록 분리 설계
     * =========================================================================
     */

    /**
     * Kafka 메시지(orderId) 기반 쿠폰 사용 처리
     */
    @Transactional
    public void useCouponByOrderId(UUID orderId){

        OrderInfo order = orderProvider.getOrder(orderId);

        UUID couponIssuedId = order.couponIssueId();
        UUID userId = order.userId();

        log.info("[SAGA] couponIssueService 사용 요청: orderId={}, couponIssueId={}, userId={}",
                orderId, couponIssuedId, userId);

        useCoupon(couponIssuedId, userId);
    }

    /**
     * Kafka 메시지(orderId) 기반 쿠폰 복원 처리
     * todo 복원도 오케스트레이션에 포함될 시 추가
     */
    @Transactional
    public void restoreCouponByOrderId(UUID orderId) {

        OrderInfo order = orderProvider.getOrder(orderId);
        UUID couponIssueId = order.couponIssueId();

        log.info("[SAGA] couponIssueService Coupon 복원 요청: orderId={}, couponIssueId={}", orderId, couponIssueId);

        restoreCoupon(couponIssueId);
    }
}

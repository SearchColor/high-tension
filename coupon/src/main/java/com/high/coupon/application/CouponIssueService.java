package com.high.coupon.application;

import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.high.coupon.application.dto.response.CouponUseResponse;
import com.high.coupon.application.dto.response.CouponValidationResponse;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.high.coupon.application.exception.CouponIssueNotFoundException;
import com.high.coupon.application.exception.CouponOutOfStockException;
import com.high.coupon.application.port.out.CouponCachePort;
import com.high.coupon.application.port.out.CouponIssueEventPort;
import com.high.coupon.application.port.out.OrderPort;
import com.high.coupon.application.port.out.dto.CouponIssueCreateMessage;
import com.high.coupon.application.port.out.dto.OrderInfo;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.exception.CouponAlreadyIssuedException;
import com.high.coupon.domain.exception.CouponNotOwnedException;
import com.high.coupon.domain.repository.CouponIssueRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponIssueService {

    // private final CouponService couponService;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponReader couponReader;

    private final OrderPort orderPort;
    private final CouponCachePort couponCachePort;

    private final CouponIssueEventPort couponIssueEventPort;

    // config
    @Value("${coupon.policy.retention-days}")
    private long retentionDays;

    /**
     * 쿠폰 발급
     * Redis 검증 -> Kafka 메시지 발송 -> 발급 응답
     * DB 트랜잭션: ReadOnly (조회만 함)!
     */
    public CouponIssueResponse issueCoupon(UUID couponId, UUID userId) {

        log.info("User {} is issued a coupon", userId);

        Coupon coupon = couponReader.getCouponById(couponId);

        long ttlSeconds = calculateTTL(coupon.getIssueEndAt());

        Long result = couponCachePort.tryIssueCoupon(
                couponId,
                userId,
                coupon.getTotalQuantity(),
                ttlSeconds
        );

        // 0 성공, -1 실패: 수량 소진, -2 실패: 이미 발급 받음
        if (Long.valueOf(-2).equals(result)) {
            throw new CouponAlreadyIssuedException();
        }
        if (Long.valueOf(-1).equals(result)) {
            throw new CouponOutOfStockException();
        }

        // Kafka로 메세지 전송 (비동기 처리)
        couponIssueEventPort.publishIssueRequest(new CouponIssueCreateMessage(userId, couponId));

        return new CouponIssueResponse(true, "쿠폰이 발급되었습니다. 지급까지 시간이 소요될 수 있습니다");
    }

    // Consumer 호출 메서드
    @Transactional
    public void saveCouponIssue(UUID couponId, UUID userId) {
        Coupon coupon = couponReader.getCouponById(couponId);
        CouponIssue couponIssue = CouponIssue.issueCoupon(coupon, userId, LocalDateTime.now());
        couponIssueRepository.save(couponIssue);

        log.info("[COUPON ISSUE Consumer] DB 저장 완료: userId={}, couponId={}", userId, couponId);
    }


    /**
     * 발급 유저 목록 데이터 Redis 데이터 만료시간 계산 메서드
     */
    private long calculateTTL(LocalDateTime issueEndAt) {

        // 일자 계산 = config(정책) + db(쿠폰 별 발급 종료일)
        LocalDateTime endAtWithBuffer = issueEndAt.plusDays(retentionDays);

        long seconds = Duration.between(LocalDateTime.now(), endAtWithBuffer).getSeconds();
        return Math.max(seconds, 0); // TTL이 지난 과거 날짜 음수 방지
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

        OrderInfo order = orderPort.getOrder(orderId);

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

        OrderInfo order = orderPort.getOrder(orderId);
        UUID couponIssueId = order.couponIssueId();

        log.info("[SAGA] couponIssueService Coupon 복원 요청: orderId={}, couponIssueId={}", orderId, couponIssueId);

        restoreCoupon(couponIssueId);
    }
}

package com.high.coupon.application;

import com.high.coupon.application.dto.response.CouponIssueResponse;
import com.high.coupon.application.dto.response.CouponUseResponse;
import com.high.coupon.application.dto.response.UserCouponResponse;
import com.high.coupon.application.exception.CouponIssueNotFoundException;
import com.high.coupon.application.exception.CouponOutOfStockException;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.entity.CouponIssue;
import com.high.coupon.domain.exception.CouponAlreadyIssuedException;
import com.high.coupon.domain.repository.CouponIssueRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponIssueService {

    // todo : 권한 검증 필요 + 주석 정리 + 고도화 필요 (발급 파트)

    private final CouponService couponService; // 쿠폰 조회용
    private final CouponIssueRepository couponIssueRepository;

    // todo : 테스트용 제거 필요
    private final AuditorAware<UUID> auditorAware;

    /**
     * 쿠폰 발급
     */
    // + UUID userId 파라미터 추가 필요
    @Transactional
    public CouponIssueResponse issueCoupon(UUID couponId) {

        Coupon coupon = couponService.getCouponById(couponId);

        // 발급자 ID 임시
        UUID userId = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new CouponAlreadyIssuedException();
        }

        /**
         * todo: 동시성 문제로 고도화 필수 로직 (발급 수량 체크)
         * 현재는 DB 조회 기반 단순 수량 체크 -> Redis 캐시 기반 + Lock 구현 필요
         */
        long issuedCount = couponIssueRepository.countByCouponId(couponId);
        if (issuedCount >= coupon.getTotalQuantity()) {
            throw new CouponOutOfStockException();
        }

        CouponIssue couponIssue = CouponIssue.issueCoupon(coupon, userId, LocalDateTime.now());
        CouponIssue savedCouponIssue = couponIssueRepository.save(couponIssue);

        return CouponIssueResponse.from(savedCouponIssue);
    }




    /**
     * ----- internal 메서드 -----
     */


    // 사용자 별 쿠폰 조회 todo 삭제 예정
//    public List<UserCouponResponse> getUserCoupons(UUID userId) {
//        List<CouponIssue> issues = couponIssueRepository.findAllByUserId(userId);
//
//        return issues.stream()
//                .map(UserCouponResponse::from)
//                .toList();
//    }

    // 사용자 별 사용 가능한 쿠폰 조회
    public List<UserCouponResponse> getAvailableUserCoupons(UUID userId) {

        List<CouponIssue> issues =
                couponIssueRepository.findAvailableByUserId(userId);

        return issues.stream()
                .map(UserCouponResponse::from)
                .toList();
    }


    // 쿠폰 사용 처리
    @Transactional
    public CouponUseResponse useCoupon(UUID couponIssueId, UUID userId){

        CouponIssue couponIssue = getCouponIssue(couponIssueId);

        couponIssue.useCoupon(userId, LocalDateTime.now());
        log.info("[INTERNAL] Coupon-Issue-Service - 쿠폰 사용처리 : couponIssueId={}, userId={}", couponIssueId, userId);
        return CouponUseResponse.from(couponIssue);
    }

    // 쿠폰 복원 처리 (주문/결제 취소)
    @Transactional
    public CouponUseResponse restoreCoupon(UUID couponIssueId) {
        CouponIssue couponIssue = getCouponIssue(couponIssueId);

        LocalDateTime now = LocalDateTime.now();
        couponIssue.restoreCoupon(now);

        log.info("[INTERNAL] Coupon-Issue-Service - 쿠폰 복원 프로세스 종료 : couponIssueId={}", couponIssueId);

        return CouponUseResponse.from(couponIssue);
    }


    /** -----------------------
     * 쿠폰 발급 이력 ID 조회 메서드
     */
    private CouponIssue getCouponIssue(UUID couponIssueId) {
        return couponIssueRepository.findById(couponIssueId)
                .orElseThrow(CouponIssueNotFoundException::new);
    }
}

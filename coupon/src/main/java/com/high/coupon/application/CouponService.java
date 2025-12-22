package com.high.coupon.application;

import com.high.coupon.application.dto.request.CouponCreateRequest;
import com.high.coupon.application.dto.response.CouponCreateResponse;
import com.high.coupon.application.dto.response.CouponDetailResponse;
import com.high.coupon.application.dto.response.CouponListResponse;
import com.high.coupon.application.exception.CouponNotFoundException;
import com.high.coupon.domain.entity.Coupon;
import com.high.coupon.domain.repository.CouponRepository;
import com.library.jpa.response.PageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    // 쿠폰 등록
    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request, UUID userId){

        log.info("User {} is creating a coupon (master 권한)", userId);

        Coupon coupon = Coupon.createCoupon(
                request.name(),
                request.description(),
                request.discountRate(),
                request.totalQuantity(),
                request.issueStartAt(),
                request.issueEndAt(),
                request.validUntil()
        );

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponCreateResponse.from(savedCoupon);
    }


    // 쿠폰 단건 조회
    public CouponDetailResponse getCouponDetail(UUID couponId){
        return couponRepository.findById(couponId)
                .map(CouponDetailResponse::from)
                .orElseThrow(CouponNotFoundException::new);
    }


    // 쿠폰 리스트 조회
    public PageResponse<CouponListResponse> getCouponPage(int page, int size, String sortBy, boolean isAsc) {
        Pageable pageable = createPageable(page, size, sortBy, isAsc);
        Page<Coupon> pageResult = couponRepository.findAll(pageable);
        Page<CouponListResponse> couponList = pageResult.map(CouponListResponse::from);
        return PageResponse.fromPage(couponList, sortBy, isAsc);
    }

    /**
     * 쿠폰 페이징
     */
    private Pageable createPageable(int page, int size, String sortBy, boolean isAsc) {
        int validatedSize = List.of(10, 20).contains(size) ? size : 10;
        int validatedPage = Math.max(page, 0);
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        // 유효한 정렬 필드
        final List<String> ALLOWED_SORTS = List.of("issueStartAt", "issueEndAt", "validUntil", "createdAt", "discountRate");
        String validatedSortBy = ALLOWED_SORTS.contains(sortBy) ? sortBy : "createdAt";

        return PageRequest.of(validatedPage, validatedSize, Sort.by(direction, validatedSortBy));
    }
}

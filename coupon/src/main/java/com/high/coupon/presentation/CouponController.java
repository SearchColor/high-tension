package com.high.coupon.presentation;

import com.high.coupon.application.CouponService;
import com.high.coupon.application.dto.request.CouponCreateRequest;
import com.high.coupon.application.dto.response.CouponCreateResponse;
import com.high.coupon.application.dto.response.CouponDetailResponse;
import com.high.coupon.application.dto.response.CouponListResponse;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Coupon", description = "쿠폰 관리 API (MASTER 권한이 필요합니다.)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;


    // 쿠폰 생성 (관리자)
    @Operation(summary = "쿠폰 생성", description = "새로운 쿠폰 생성")
    @PreAuthorize("hasRole('MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponCreateResponse>> createCoupon(
            @RequestBody @Valid CouponCreateRequest request){

        UUID userId = SecurityContextUtil.getCurrentUserId();
        var response = couponService.createCoupon(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 발행 쿠폰 상세 조회 (관리자)
    @Operation(summary = "쿠폰 상세 조회", description = "생성된 쿠폰 상세 조회")
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponById(
            @PathVariable("couponId") UUID couponId) {

        var response = couponService.getCouponDetail(couponId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 발행 쿠폰 리스트 조회 (관리자)
    /**
     * 생성 쿠폰 리스트 (관리자용) default 10 & 20 paging
     * @param sortBy 정렬 필드 (issueStartAt, validUntil, createdAt, discountRate)
     * @param isAsc 오름차순/내림차순 default createdAt DESC
     * @return 페이징 된 리스트
     */
    @Operation(summary = "쿠폰 리스트 조회", description = "생성 쿠폰 리스트 조회")
    @PreAuthorize("hasRole('MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CouponListResponse>>> getCouponPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean isAsc
    ) {
        var response = couponService.getCouponPage(page, size, sortBy, isAsc);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

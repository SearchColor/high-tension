package com.high.user.infrastructure.client;

import com.high.user.application.dto.response.CouponResponse;
import com.high.user.domain.service.CouponClient;
import com.high.user.infrastructure.client.dto.UserCouponResponse;
import com.library.module.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * CouponClient 구현체 (Adapter Pattern)
 * Feign Client를 사용하여 Coupon Service와 통신하고
 * Infrastructure DTO를 Application DTO로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponServiceClientAdaptor implements CouponClient {

    private final CouponServiceClient couponServiceClient;

    @Override
    public List<CouponResponse> getUserCoupons(UUID userId) {
        try {
            log.info("Fetching user coupons from Coupon Service: userId={}", userId);

            ApiResponse<List<UserCouponResponse>> response =
                    couponServiceClient.getUserCoupons(userId);

            if (response == null || response.data() == null) {
                log.warn("Coupon Service returned null response for userId={}", userId);
                return Collections.emptyList();
            }

            // Infrastructure DTO → Application DTO 변환
            List<CouponResponse> coupons = response.data().stream()
                    .map(this::convertToApplicationDto)
                    .toList();

            log.info("User coupons fetched successfully: userId={}, count={}",
                    userId, coupons.size());

            return coupons;

        } catch (Exception e) {
            // Fallback: Feign 실패 시 빈 리스트 반환
            log.error("Failed to fetch coupons from Coupon Service: userId={}, error={}",
                    userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Infrastructure DTO → Application DTO 변환
     */
    private CouponResponse convertToApplicationDto(UserCouponResponse infrastructureDto) {
        return new CouponResponse(
                infrastructureDto.couponIssueId(),
                infrastructureDto.couponId(),
                infrastructureDto.couponName(),
                infrastructureDto.discountRate(),
                infrastructureDto.validStartAt(),
                infrastructureDto.validEndAt(),
                infrastructureDto.isUsed()
        );
    }
}
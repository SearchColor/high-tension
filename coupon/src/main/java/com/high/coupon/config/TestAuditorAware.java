package com.high.coupon.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/**
 * 개발용 임시 값 config -> 유저 쪽 개발되면 삭제
 */
@Component
public class TestAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("test-user"); // 테스트 유저 임시값 -> UUID 변경 필요
    }
}

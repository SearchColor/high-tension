package com.high.coupon.infrastructure.config;

import com.high.coupon.infrastructure.context.MessageContext;
import com.library.security.util.SecurityContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        if (MessageContext.hasContext()) {
            template.header("X-User-Id", MessageContext.getUserId().toString());
            template.header("X-User-Role", MessageContext.getRole());
            return;
        }

        try {
            template.header("X-User-Id", SecurityContextUtil.getCurrentUserIdAsString());
            template.header("X-User-Role", SecurityContextUtil.getCurrentUserRole());
        } catch (Exception ignored) {}
    }

}

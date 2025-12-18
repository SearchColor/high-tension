package com.high.order.infrastructure.config.feign;

import com.high.order.infrastructure.context.MessageContext;
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
        // 1. Kafka 메시지 기반 Context 우선
        if (MessageContext.hasContext()) {
            template.header("X-User-Id", MessageContext.getUserId().toString());
            template.header("X-User-Role", MessageContext.getRole());
            return;
        }

        // 2. HTTP SecurityContext fallback
        try {
            template.header("X-User-Id", SecurityContextUtil.getCurrentUserIdAsString());
            template.header("X-User-Role", SecurityContextUtil.getCurrentUserRole());
        } catch (Exception ignored) {}

    }
// 직렬화 역직렬화 오류시 필요
//    @Bean
//    public Decoder feignDecoder() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        HttpMessageConverters converters = new HttpMessageConverters(
//            new MappingJackson2HttpMessageConverter(objectMapper)
//        );
//
//        return new SpringDecoder(() -> converters);
//    }
//
//    @Bean
//    public Encoder feignEncoder() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        HttpMessageConverters converters = new HttpMessageConverters(
//            new MappingJackson2HttpMessageConverter(objectMapper)
//        );
//
//        return new SpringEncoder(() -> converters);
//    }


}

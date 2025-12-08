package com.high.order.infrastructure.config;

import com.high.order.infrastructure.exception.ProductNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {


    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getProductById")) {
                    log.info("상품이 존재하지 않습니다.");
                    return new ProductNotFoundException();
                }
                break;
            default:
                log.info("feignClient 통신 중 에러 발생");
                return new Exception(response.reason());
        }

        return null;
    }
}

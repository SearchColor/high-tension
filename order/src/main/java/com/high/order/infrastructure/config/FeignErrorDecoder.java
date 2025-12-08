package com.high.order.infrastructure.config;

import com.high.order.infrastructure.exception.ProductNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
                    return new ProductNotFoundException();
                }
                break;
            default:
                return new Exception(response.reason());
        }

        return null;
    }
}

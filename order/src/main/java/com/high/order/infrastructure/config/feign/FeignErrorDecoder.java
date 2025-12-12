package com.high.order.infrastructure.config.feign;

import com.high.order.infrastructure.exception.FeignCommunicationErrorException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {


    @Override
    public Exception decode(String methodKey, Response response) {

        log.warn("Feign 통신 에러 발생: methodKey={}, status={}, reason={}",
            methodKey, response.status(), response.reason());

        return new FeignCommunicationErrorException();

    }
}

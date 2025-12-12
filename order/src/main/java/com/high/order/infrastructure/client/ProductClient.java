package com.high.order.infrastructure.client;

import com.high.order.application.dto.external.ProductResponse;
import com.high.order.application.service.ProductService;
import com.high.order.infrastructure.config.FeignConfig;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", configuration =  FeignConfig.class)
public interface ProductClient extends ProductService {

    @GetMapping("/api/v1/products/{productId}")
    ApiResponse<ProductResponse> getProductById(
        @PathVariable("productId") UUID productId);

}

package com.high.order.infrastructure.client;

import com.high.order.application.dto.external.ProductResponse;
import com.high.order.application.service.ProductService;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient extends ProductService {

    @GetMapping("/api/v1/products/{productId}")
    ResponseEntity<ApiResponse<ProductResponse>> getProductById(
        @PathVariable("productId") UUID productId);

}

package com.high.order.application.service;

import com.high.order.application.dto.external.ProductResponse;
import com.library.module.response.ApiResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {

    ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("productId") UUID productId);

}

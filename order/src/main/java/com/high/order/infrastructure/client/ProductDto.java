package com.high.order.infrastructure.client;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ProductDto(
    UUID productId,
    String name,
    Integer price,
    String category,
    LocalDateTime createdAt,
    UUID producerId


    ) {

    //임시 더미데이터
    public static ProductDto init() {
        return new ProductDto(
            UUID.randomUUID(),
            "sample1",
            ((int)(Math.random() * 9 + 1)) * 1000,
            "문구",
            LocalDateTime.now(),
            UUID.randomUUID()
        );
    }
}

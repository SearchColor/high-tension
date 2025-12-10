package com.high.order.application.dto.external;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    int price,
    String category,
    UUID seller,
    LocalDateTime createdAt,
    UUID createdBy,
    LocalDateTime updatedAt,
    UUID updatedBy,
    LocalDateTime deletedAt,
    UUID deletedBy
) {

}

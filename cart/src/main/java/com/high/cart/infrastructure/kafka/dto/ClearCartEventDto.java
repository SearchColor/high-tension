package com.high.cart.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClearCartEventDto {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("sagaId")
    private String sagaId;

}

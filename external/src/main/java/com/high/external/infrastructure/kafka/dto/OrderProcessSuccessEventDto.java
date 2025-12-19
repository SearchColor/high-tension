package com.high.external.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessSuccessEventDto {

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("paidAmount")
    private Integer paidAmount;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("orderItemNameList")
    private List<String> orderItemNameList;

}

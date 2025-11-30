package com.high.order.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ProductOrderCreateRequest(

    @NotNull(message = "상품 선택은 필수입니다.")
    UUID productId,

    UUID couponId,

    @NotNull(message = "수량 입력은 필수입니다.")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
    Integer quantity,

    @NotBlank(message = "수령자 정보는 필수입니다")
    String recipient,

    @JsonProperty("recipient_contact")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
        regexp = "^(010)(-?\\d{4})(-?\\d{4})$",
        message = "전화번호 형식은 010-XXXX-XXXX 입니다."
    )
    String recipientContact,

    @JsonProperty("delivery_address")
    @NotBlank(message = "주소는 필수입니다")
    String deliveryAddress,

    @JsonProperty("detail_address")
    String detailAddress,

    @JsonProperty("request_message")
    String requestMessage
) {


}

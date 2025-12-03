package com.high.order.application.dto.request;

import java.util.Optional;

public record OrderUpdateRequest(

    Optional<String> recipient,
    Optional<String> recipientContact,
    Optional<String> deliveryAddress,
    Optional<String> detailAddress,
    Optional<String> requestMessage

) {}

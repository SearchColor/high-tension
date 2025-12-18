package com.high.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeOperation {
    REGISTRATION("패스키 등록"),
    AUTHENTICATION("패스키 인증");

    private final String description;
}
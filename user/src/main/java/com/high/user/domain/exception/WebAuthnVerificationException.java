package com.high.user.domain.exception;

import com.library.module.exception.CustomException;

public class WebAuthnVerificationException extends CustomException {
    public WebAuthnVerificationException() {
        super(UserErrorCode.WEBAUTHN_VERIFICATION_FAILED);
    }

    public WebAuthnVerificationException(String message) {
        super(UserErrorCode.WEBAUTHN_VERIFICATION_FAILED);
    }
}
-- Passkey 테이블 생성
CREATE TABLE IF NOT EXISTS p_passkeys (
    passkey_id BINARY(16) NOT NULL PRIMARY KEY COMMENT '패스키 고유 ID (UUID)',
    user_id BINARY(16) NOT NULL COMMENT '사용자 ID (FK)',
    credential_id VARCHAR(512) NOT NULL UNIQUE COMMENT 'WebAuthn Credential ID (Base64 URL-safe)',
    public_key_cose TEXT NOT NULL COMMENT 'COSE 형식 공개키 (Base64 URL-safe)',
    signature_count BIGINT NOT NULL DEFAULT 0 COMMENT '서명 카운터 (클론 감지용)',
    aaguid VARCHAR(36) COMMENT 'Authenticator AAGUID',
    credential_name VARCHAR(100) COMMENT '사용자 지정 패스키 이름',
    last_used_at TIMESTAMP NULL COMMENT '마지막 사용 일시',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    deleted_at TIMESTAMP NULL COMMENT '삭제 일시 (Soft Delete)',
    deleted_by VARCHAR(100) COMMENT '삭제자',

    INDEX idx_passkeys_user_id (user_id),
    INDEX idx_passkeys_credential_id (credential_id),
    INDEX idx_passkeys_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='패스키 정보 (WebAuthn)';

-- Passkey Challenge 테이블 생성
CREATE TABLE IF NOT EXISTS p_passkey_challenges (
    challenge_id BINARY(16) NOT NULL PRIMARY KEY COMMENT 'Challenge 고유 ID (UUID)',
    challenge VARCHAR(512) NOT NULL UNIQUE COMMENT 'Challenge 값 (Base64 URL-safe)',
    user_id BINARY(16) COMMENT '사용자 ID (인증 시 null 가능)',
    operation VARCHAR(20) NOT NULL COMMENT '작업 유형 (REGISTRATION, AUTHENTICATION)',
    request_data TEXT COMMENT '요청 데이터 (JSON 직렬화)',
    expires_at TIMESTAMP NOT NULL COMMENT '만료 일시',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    deleted_at TIMESTAMP NULL COMMENT '삭제 일시',
    deleted_by VARCHAR(100) COMMENT '삭제자',

    INDEX idx_challenges_challenge (challenge),
    INDEX idx_challenges_expires_at (expires_at),
    INDEX idx_challenges_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='패스키 Challenge 임시 저장 (TTL 5분)';
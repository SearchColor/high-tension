package com.high.user.domain.entity;

import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_passkeys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Passkey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "passkey_id")
    private UUID passkeyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "credential_id", nullable = false, unique = true, length = 512)
    private String credentialId;  // Base64URL encoded

    @Column(name = "public_key_cose", nullable = false, columnDefinition = "TEXT")
    private String publicKeyCose;  // Base64URL encoded COSE key

    @Column(name = "signature_count", nullable = false)
    private Long signatureCount;

    @Column(name = "aaguid", length = 36)
    private String aaguid;  // Authenticator attestation GUID

    @Column(name = "credential_name", length = 100)
    private String credentialName;  // 사용자 지정 패스키 이름

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Private constructor
    private Passkey(UUID userId, String credentialId, String publicKeyCose,
                    Long signatureCount, String aaguid) {
        this.userId = userId;
        this.credentialId = credentialId;
        this.publicKeyCose = publicKeyCose;
        this.signatureCount = signatureCount;
        this.aaguid = aaguid;
    }

    // Factory method
    public static Passkey create(UUID userId, String credentialId, String publicKeyCose,
                                  Long signatureCount, String aaguid) {
        if (credentialId == null || credentialId.isBlank()) {
            throw new IllegalArgumentException("Credential ID는 필수입니다");
        }
        if (publicKeyCose == null || publicKeyCose.isBlank()) {
            throw new IllegalArgumentException("Public Key는 필수입니다");
        }
        return new Passkey(userId, credentialId, publicKeyCose, signatureCount, aaguid);
    }

    // Business logic
    public void updateSignatureCount(Long newCount) {
        this.signatureCount = newCount;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateCredentialName(String name) {
        if (name != null && name.length() > 100) {
            throw new IllegalArgumentException("패스키 이름은 100자 이하여야 합니다");
        }
        this.credentialName = name;
    }
}
package com.high.user.infrastructure.config;

import com.high.user.infrastructure.webauthn.WebAuthnCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebAuthnConfig {

    @Value("${webauthn.relying-party.id}")
    private String rpId;

    @Value("${webauthn.relying-party.name}")
    private String rpName;

    @Value("${webauthn.relying-party.origins}")
    private String origins;

    private final WebAuthnCredentialRepository credentialRepository;

    @Bean
    public RelyingPartyIdentity relyingPartyIdentity() {
        return RelyingPartyIdentity.builder()
            .id(rpId)
            .name(rpName)
            .build();
    }

    @Bean
    public RelyingParty relyingParty(RelyingPartyIdentity rpIdentity) {
        Set<String> originSet = Arrays.stream(origins.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(credentialRepository)
            .origins(originSet)
            .allowOriginPort(true)  // 개발 환경용
            .build();
    }
}
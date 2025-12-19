package com.high.user.infrastructure.webauthn;

import com.high.user.domain.entity.Passkey;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.InvalidCredentialFormatException;
import com.high.user.domain.repository.PasskeyRepository;
import com.high.user.domain.repository.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.exception.Base64UrlException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebAuthnCredentialRepository implements CredentialRepository {

    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        // username = email
        return userRepository.findByEmailAndDeletedAtIsNull(username)
            .map(user -> passkeyRepository.findByUserIdAndDeletedAtIsNull(user.getUserId()))
            .orElse(Collections.emptyList())
            .stream()
            .map(passkey -> {
                try {
                    return PublicKeyCredentialDescriptor.builder()
                        .id(ByteArray.fromBase64Url(passkey.getCredentialId()))
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build();
                } catch (Base64UrlException e) {
                    log.error("Failed to parse credential ID: {}", passkey.getCredentialId(), e);
                    throw new InvalidCredentialFormatException();
                }
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByEmailAndDeletedAtIsNull(username)
            .map(user -> new ByteArray(user.getUserId().toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        String userIdString = new String(userHandle.getBytes(), StandardCharsets.UTF_8);
        UUID userId = UUID.fromString(userIdString);
        return userRepository.findByIdAndDeletedAtIsNull(userId)
            .map(User::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        String credentialIdBase64 = credentialId.getBase64Url();
        return passkeyRepository.findByCredentialId(credentialIdBase64)
            .map(passkey -> {
                try {
                    return RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(ByteArray.fromBase64Url(passkey.getPublicKeyCose()))
                        .signatureCount(passkey.getSignatureCount())
                        .build();
                } catch (Base64UrlException e) {
                    log.error("Failed to parse public key: {}", passkey.getPublicKeyCose(), e);
                    throw new InvalidCredentialFormatException();
                }
            });
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        String credentialIdBase64 = credentialId.getBase64Url();
        return passkeyRepository.findByCredentialId(credentialIdBase64)
            .map(passkey -> {
                try {
                    ByteArray userHandle = new ByteArray(
                        passkey.getUserId().toString().getBytes(StandardCharsets.UTF_8));
                    return RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(ByteArray.fromBase64Url(passkey.getPublicKeyCose()))
                        .signatureCount(passkey.getSignatureCount())
                        .build();
                } catch (Base64UrlException e) {
                    log.error("Failed to parse public key: {}", passkey.getPublicKeyCose(), e);
                    throw new InvalidCredentialFormatException();
                }
            })
            .map(Set::of)
            .orElse(Collections.emptySet());
    }
}
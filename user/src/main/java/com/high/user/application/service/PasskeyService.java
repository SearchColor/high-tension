package com.high.user.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.user.application.dto.request.*;
import com.high.user.application.dto.response.*;
import com.high.user.domain.entity.Passkey;
import com.high.user.domain.entity.PasskeyChallenge;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.*;
import com.high.user.domain.repository.PasskeyChallengeRepository;
import com.high.user.domain.repository.PasskeyRepository;
import com.high.user.domain.repository.UserRepository;
import com.high.user.domain.service.TokenProvider;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasskeyService {

    private final RelyingParty relyingParty;
    private final PasskeyRepository passkeyRepository;
    private final PasskeyChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Value("${webauthn.challenge.ttl-minutes:5}")
    private int challengeTtlMinutes;

    /**
     * 패스키 등록 시작 (Challenge 생성)
     */
    @Transactional
    public PasskeyRegistrationStartResponse startRegistration(UUID userId,
            PasskeyRegistrationStartRequest request) {
        log.info("[Passkey] Registration start for userId: {}", userId);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(UserNotFoundException::new);

        // UserIdentity 생성
        UserIdentity userIdentity = UserIdentity.builder()
            .name(user.getEmail())
            .displayName(user.getName())
            .id(new ByteArray(userId.toString().getBytes(StandardCharsets.UTF_8)))
            .build();

        // Registration Options 생성
        // Note: excludeCredentials는 CredentialRepository에서 자동으로 처리됨 (Yubico 2.5.x)
        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
            .user(userIdentity)
            .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                .residentKey(ResidentKeyRequirement.PREFERRED)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build())
            .timeout(300000L)  // 5분
            .build();

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(registrationOptions);

        // Challenge 저장 (요청 데이터 포함)
        String requestDataJson = serializeToJson(options);
        PasskeyChallenge challenge = PasskeyChallenge.createForRegistration(
            options.getChallenge().getBase64Url(),
            userId,
            requestDataJson,
            challengeTtlMinutes);
        challengeRepository.save(challenge);

        try {
            String jsonString = options.toCredentialsCreateJson();
            return PasskeyRegistrationStartResponse.of(objectMapper.readTree(jsonString));
        } catch (JsonProcessingException e) {
            log.error("[Passkey] Failed to serialize registration options", e);
            throw new PasskeySerializationException();
        }
    }

    /**
     * 패스키 등록 완료
     */
    @Transactional
    public PasskeyRegistrationFinishResponse finishRegistration(UUID userId,
            PasskeyRegistrationFinishRequest request) {
        log.info("[Passkey] Registration finish for userId: {}", userId);

        // Credential 응답 파싱
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc;
        try {
            pkc = PublicKeyCredential.parseRegistrationResponseJson(request.credentialJson());
        } catch (Exception e) {
            log.error("[Passkey] Failed to parse registration response", e);
            throw new WebAuthnVerificationException("패스키 응답 파싱에 실패했습니다");
        }

        // Challenge 조회 및 검증
        String challengeBase64 = pkc.getResponse().getClientData().getChallenge().getBase64Url();
        PasskeyChallenge challenge = challengeRepository.findByChallenge(challengeBase64)
            .orElseThrow(InvalidChallengeException::new);

        if (challenge.isExpired()) {
            challengeRepository.deleteByChallenge(challengeBase64);
            throw new InvalidChallengeException();
        }

        if (!challenge.getUserId().equals(userId)) {
            throw new InvalidChallengeException();
        }

        // 저장된 요청 데이터 복원
        PublicKeyCredentialCreationOptions originalRequest = deserializeCreationOptions(
            challenge.getRequestData());

        // WebAuthn 검증
        RegistrationResult result;
        try {
            result = relyingParty.finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(originalRequest)
                    .response(pkc)
                    .build());
        } catch (RegistrationFailedException e) {
            log.error("[Passkey] Registration verification failed", e);
            challengeRepository.deleteByChallenge(challengeBase64);
            throw new WebAuthnVerificationException("패스키 등록 검증에 실패했습니다");
        }

        // 중복 등록 방지: credentialId가 이미 존재하는지 확인
        String credentialIdBase64 = result.getKeyId().getId().getBase64Url();
        if (passkeyRepository.findByCredentialId(credentialIdBase64).isPresent()) {
            log.warn("[Passkey] Duplicate credential registration attempt: userId={}, credentialId={}",
                     userId, credentialIdBase64);
            throw new PasskeyAlreadyExistsException();
        }

        // Passkey Entity 생성 및 저장
        // AAGUID를 Hex 문자열로 변환
        String aaguidString = result.getAaguid().getHex();

        Passkey passkey = Passkey.create(
            userId,
            result.getKeyId().getId().getBase64Url(),
            result.getPublicKeyCose().getBase64Url(),
            result.getSignatureCount(),
            aaguidString);

        if (request.credentialName() != null) {
            passkey.updateCredentialName(request.credentialName());
        }

        Passkey saved = passkeyRepository.save(passkey);

        // Challenge 삭제
        challengeRepository.deleteByChallenge(challengeBase64);

        log.info("[Passkey] Registration completed: passkeyId={}", saved.getPasskeyId());

        return PasskeyRegistrationFinishResponse.from(saved);
    }

    /**
     * 패스키 인증 시작 (Challenge 생성)
     */
    @Transactional
    public PasskeyAuthenticationStartResponse startAuthentication(
            PasskeyAuthenticationStartRequest request) {
        log.info("[Passkey] Authentication start for email: {}", request.email());

        StartAssertionOptions.StartAssertionOptionsBuilder optionsBuilder =
            StartAssertionOptions.builder()
                .timeout(300000L)  // 5분
                .userVerification(UserVerificationRequirement.PREFERRED);

        // email이 제공된 경우 해당 사용자의 credential만 허용
        UUID userId = null;
        if (request.email() != null && !request.email().isBlank()) {
            User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(UserNotFoundException::new);
            userId = user.getUserId();
            optionsBuilder.username(request.email());
        }

        AssertionRequest assertionRequest = relyingParty.startAssertion(optionsBuilder.build());

        // Challenge 저장
        String requestDataJson = serializeToJson(assertionRequest);
        PasskeyChallenge challenge = PasskeyChallenge.createForAuthentication(
            assertionRequest.getPublicKeyCredentialRequestOptions().getChallenge().getBase64Url(),
            userId,
            requestDataJson,
            challengeTtlMinutes);
        challengeRepository.save(challenge);

        try {
            String jsonString = assertionRequest.toCredentialsGetJson();
            return PasskeyAuthenticationStartResponse.of(String.valueOf(objectMapper.readTree(jsonString)));
        } catch (JsonProcessingException e) {
            log.error("[Passkey] Failed to serialize authentication options", e);
            throw new PasskeySerializationException();
        }
    }
    /**
     * 패스키 인증 완료 (JWT 토큰 발급)
     */
    @Transactional
    public TokenResponse finishAuthentication(PasskeyAuthenticationFinishRequest request) {
        log.info("[Passkey] Authentication finish");

        // Credential 응답 파싱
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc;
        try {
            pkc = PublicKeyCredential.parseAssertionResponseJson(request.credentialJson());
        } catch (Exception e) {
            log.error("[Passkey] Failed to parse assertion response", e);
            throw new WebAuthnVerificationException("패스키 응답 파싱에 실패했습니다");
        }

        // Challenge 조회 및 검증
        String challengeBase64 = pkc.getResponse().getClientData().getChallenge().getBase64Url();
        PasskeyChallenge challenge = challengeRepository.findByChallenge(challengeBase64)
            .orElseThrow(InvalidChallengeException::new);

        if (challenge.isExpired()) {
            challengeRepository.deleteByChallenge(challengeBase64);
            throw new InvalidChallengeException();
        }

        // 저장된 요청 데이터 복원
        AssertionRequest originalRequest = deserializeAssertionRequest(challenge.getRequestData());

        // WebAuthn 검증
        AssertionResult result;
        try {
            result = relyingParty.finishAssertion(
                FinishAssertionOptions.builder()
                    .request(originalRequest)
                    .response(pkc)
                    .build());
        } catch (AssertionFailedException e) {
            log.error("[Passkey] Assertion verification failed", e);
            challengeRepository.deleteByChallenge(challengeBase64);
            throw new WebAuthnVerificationException("패스키 인증 검증에 실패했습니다");
        }

        if (!result.isSuccess()) {
            throw new WebAuthnVerificationException();
        }

        // Passkey 조회 및 Signature Count Downgrade 공격 방어
        String credentialIdBase64 = pkc.getId().getBase64Url();
        Passkey passkey = passkeyRepository.findByCredentialId(credentialIdBase64)
            .orElseThrow(PasskeyNotFoundException::new);

        // Signature Count 검증 - Downgrade 공격 방어
        // WebAuthn 표준: 새 카운터 > 기존 카운터 (또는 0이면 카운터 미지원)
        if (result.getSignatureCount() > 0 && passkey.getSignatureCount() > 0) {
            if (result.getSignatureCount() <= passkey.getSignatureCount()) {
                log.warn("[Passkey] Signature count downgrade attack detected: " +
                         "userId={}, passkeyId={}, oldCount={}, newCount={}",
                         passkey.getUserId(), passkey.getPasskeyId(),
                         passkey.getSignatureCount(), result.getSignatureCount());
                throw new WebAuthnVerificationException();
            }
        }

        passkey.updateSignatureCount(result.getSignatureCount());
        passkeyRepository.save(passkey);

        // 사용자 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(passkey.getUserId())
            .orElseThrow(UserNotFoundException::new);

        // 계정 상태 확인
        if (!user.getIsActive()) {
            throw new InactiveAccountException();
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user);

        // JWT 토큰 발급
        String accessToken = tokenProvider.createAccessToken(user.getUserId(), user.getRole().name());
        String refreshToken = tokenProvider.createRefreshToken(user.getUserId());
        refreshTokenService.saveRefreshToken(user.getUserId(), refreshToken);

        // Challenge 삭제
        challengeRepository.deleteByChallenge(challengeBase64);

        log.info("[Passkey] Authentication completed: userId={}", user.getUserId());

        return TokenResponse.of(accessToken, refreshToken, tokenProvider.getAccessTokenValidity());
    }

    /**
     * 사용자의 패스키 목록 조회
     */
    public List<PasskeyListResponse> getPasskeys(UUID userId) {
        log.info("[Passkey] Get passkeys for userId: {}", userId);

        List<Passkey> passkeys = passkeyRepository.findByUserIdAndDeletedAtIsNull(userId);
        return PasskeyListResponse.fromList(passkeys);
    }

    /**
     * 패스키 Credential Name 수정
     */
    @Transactional
    public PasskeyListResponse updateCredentialName(UUID userId, UUID passkeyId,
                                                     PasskeyUpdateRequest request) {
        log.info("[Passkey] Update credential name: userId={}, passkeyId={}", userId, passkeyId);

        // 1. 패스키 조회 (Soft Delete 제외)
        Passkey passkey = passkeyRepository.findByIdAndDeletedAtIsNull(passkeyId)
            .orElseThrow(PasskeyNotFoundException::new);

        // 2. 본인 소유 검증 (보안상 404 응답)
        if (!passkey.getUserId().equals(userId)) {
            throw new PasskeyNotFoundException();
        }

        // 3. Credential Name 업데이트
        passkey.updateCredentialName(request.credentialName());

        // 4. 저장
        Passkey updated = passkeyRepository.save(passkey);

        log.info("[Passkey] Credential name updated: passkeyId={}, newName={}",
                 passkeyId, request.credentialName());

        return PasskeyListResponse.from(updated);
    }

    /**
     * 패스키 삭제
     */
    @Transactional
    public void deletePasskey(UUID userId, UUID passkeyId) {
        log.info("[Passkey] Delete passkey: userId={}, passkeyId={}", userId, passkeyId);

        Passkey passkey = passkeyRepository.findByIdAndDeletedAtIsNull(passkeyId)
            .orElseThrow(PasskeyNotFoundException::new);

        if (!passkey.getUserId().equals(userId)) {
            throw new PasskeyNotFoundException();
        }

        passkey.softDelete(userId);
        passkeyRepository.save(passkey);

        log.info("[Passkey] Deleted: passkeyId={}", passkeyId);
    }

    // Helper methods
    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[Passkey] JSON serialization failed", e);
            throw new PasskeySerializationException();
        }
    }

    private PublicKeyCredentialCreationOptions deserializeCreationOptions(String json) {
        try {
            return PublicKeyCredentialCreationOptions.fromJson(json);
        } catch (Exception e) {
            log.error("[Passkey] Failed to deserialize creation options", e);
            throw new PasskeySerializationException();
        }
    }

    private AssertionRequest deserializeAssertionRequest(String json) {
        try {
            return AssertionRequest.fromJson(json);
        } catch (Exception e) {
            log.error("[Passkey] Failed to deserialize assertion request", e);
            throw new PasskeySerializationException();
        }
    }
}

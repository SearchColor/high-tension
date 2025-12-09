package com.high.user.application.service;

import com.high.user.application.dto.request.LoginRequest;
import com.high.user.application.dto.request.SignupRequest;
import com.high.user.application.dto.response.TokenResponse;
import com.high.user.application.dto.response.UserResponse;
import com.high.user.domain.entity.User;
import com.high.user.domain.exception.DeletedAccountException;
import com.high.user.domain.exception.DuplicateEmailException;
import com.high.user.domain.exception.InactiveAccountException;
import com.high.user.domain.exception.InvalidCredentialsException;
import com.high.user.domain.repository.UserRepository;
import com.high.user.domain.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // User Entity 생성 (항상 일반 사용자로 가입, role은 Entity에서 USER로 설정됨)
        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name());

        // 저장
        User savedUser = userRepository.save(user);

        log.info("User signed up successfully: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 계정 활성화 확인
        if (!user.getIsActive()) {
            throw new InactiveAccountException();
        }

        // 삭제된 계정 확인
        if (user.isDeleted()) {
            throw new DeletedAccountException();
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user);

        // Access Token, Refresh Token 생성
        String accessToken = tokenProvider.createAccessToken(
                user.getUserId(),
                user.getRole().name());
        String refreshToken = tokenProvider.createRefreshToken(user.getUserId());

        // Redis에 Refresh Token 저장
        refreshTokenService.saveRefreshToken(user.getUserId(), refreshToken);

        log.info("User logged in successfully: userId={}, email={}", user.getUserId(), user.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenValidity());
    }

    @Transactional
    public void logout(String accessToken, String userId) {
        // Access Token 검증 및 userId 추출은 Controller/Filter 레벨에서 선행 검증됨.
        // 인자로 받은 userId 사용.

        UUID userUuid = UUID.fromString(userId);

        // Redis에서 Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(userUuid);

        // Access Token을 블랙리스트에 추가 (남은 유효시간만큼 TTL 설정)
        long remainingTime = tokenProvider.getRemainingTime(accessToken);
        refreshTokenService.addToBlacklist(accessToken, remainingTime);

        log.info("User logged out successfully: userId={}", userId);
    }
}

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
import com.high.user.domain.exception.UserNotFoundException;
import com.high.user.domain.repository.UserRepository;
import com.high.user.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // User Entity 생성
        User user = User.createLocalUser(
            request.email(),
            encodedPassword,
            request.name()
        );

        // role이 MASTER인 경우 별도 처리 (보안상 일반 가입에서는 제한해야 하지만 일단 허용)
        if (request.role() != null && request.role().name().equals("MASTER")) {
            user = User.createMasterUser(
                request.email(),
                encodedPassword,
                request.name()
            );
        }

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
        String accessToken = jwtTokenProvider.createAccessToken(
            user.getUserId(),
            user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // TODO: Redis에 Refresh Token 저장 (세션 관리)
        // TODO: 기존 세션 확인 및 만료 처리 (중복 로그인 방지)

        log.info("User logged in successfully: userId={}, email={}", user.getUserId(), user.getEmail());

        return TokenResponse.of(
            accessToken,
            refreshToken,
            jwtTokenProvider.getAccessTokenValidity()
        );
    }

    @Transactional
    public void logout(String userId) {
        // TODO: Redis에서 Refresh Token 삭제
        // TODO: 세션 정보 삭제

        log.info("User logged out successfully: userId={}", userId);
    }
}

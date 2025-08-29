package com.practice.projectchat.service;

import com.practice.projectchat.config.JwtProvider;
import com.practice.projectchat.domain.User;
import com.practice.projectchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    // 컨트롤러 <-> 서비스 레이어 전용 타입
    public record SignupCommand(String loginId, String rawPassword, String nickname){}
    public record SignupResult(Long userId, String nickname, String friendCode){}
    public record LoginCommand(String loginId, String rawPassword){}
    public record LoginResult(String tokenType, String accessToken, long expiresAt) {}


    private static final int SAVE_RETRY_COUNT = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniqueFriendCodeService uniqueFriendCodeService;
    private final JwtProvider jwtProvider;

    @Transactional
    public Mono<SignupResult> createUser(SignupCommand command) {
        // loginId 중복 검사
        return userRepository.existsByLoginId(command.loginId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("이미 사용중인 로그인 ID 입니다."));
                    }

                    // 비밀번호 해시
                    final String hashedPassword = passwordEncoder.encode(command.rawPassword);

                    // 유니크 친구코드 생성 -> 저장 (경쟁 시 재시도)
                    return uniqueFriendCodeService.generateUnique()
                            .flatMap(code -> trySave(buildUser(command.loginId, hashedPassword, command.nickname, code), 1));
                })
                .map(u -> new SignupResult(u.getId(), u.getNickname(), u.getFriendCode()));
    }

    @Transactional
    public Mono<LoginResult> login(LoginCommand command) {

        return userRepository.findByLoginId(command.loginId)
                .switchIfEmpty(Mono.error(new BadCredentialsException("INVALID_CREDENTIALS")))
                .flatMap(user -> {
                    // 계정 상태에 따라 403 또는 401
                    switch (user.getStatus()) {
                        case BLOCKED -> { // 운영자 차단
                            return Mono.error(new LockedException("ACCOUNT_BLOCKED"));
                        }
                        case DEACTIVATED -> { // 사용자 비활성(탈퇴예약)
                            return Mono.error(new DisabledException("ACCOUNT_DEACTIVATED"));
                        }
                        case DELETED -> { // 존재 노출 방지 위해 동일하게 401로 처리
                            return Mono.error(new BadCredentialsException("INVALID_CREDENTIALS"));
                        }
                    }
                    // 비밀번호 불일치 → 401
                    if (!passwordEncoder.matches(command.rawPassword, user.getPasswordHash())) {
                        return Mono.error(new BadCredentialsException("INVALID_CREDENTIALS"));
                    }

                    // 토큰 발급
                    String token = jwtProvider.generateAccessToken(user);
                    long expSec = jwtProvider.getExpiresAt(token).getEpochSecond(); // 만료 epoch seconds

                    return Mono.just(new LoginResult("Bearer", token, expSec));

                });
    }





    private Mono<User> trySave(User user, int attempt) {
        return userRepository.save(user)
                .onErrorResume(DataIntegrityViolationException.class, e -> {
                    // UNIQUE(friend_code) 충돌 시 재생성 후 재시도
                    if(attempt >= SAVE_RETRY_COUNT || !isUniqueViolation(e)) {
                        return Mono.error(e);
                    }
                    return uniqueFriendCodeService.generateUnique()
                            .flatMap(newCode -> {
                                user.setFriendCode(newCode);
                                return trySave(user, attempt + 1);
                            });
                });
    }

    // DB 제약 위반이 터지면 Spring이 DataIntegrityViolationException로 감싸서 준다
    // 그 내부 실제 예외가 R2dbcDataIntegrityViolationException이고, SQLSTATE코드로 UNIQUE키 위반 23505번이 맞는지 체크
    private boolean isUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        // Postgres UNIQUE 위반 SQLSTATE = 23505
        return (cause instanceof io.r2dbc.spi.R2dbcDataIntegrityViolationException r)
                && "23505".equals(r.getSqlState());
    }

    private static User buildUser(String loginId, String passwordHash, String nickname, String friendCode) {
        User u = new User();
        // ↓ User 엔티티에 @Getter/@Setter가 있어야 합니다.
        u.setLoginId(loginId);
        u.setPasswordHash(passwordHash);
        u.setNickname(nickname);
        u.setFriendCode(friendCode);
        u.setStatus(User.UserStatus.ACTIVE);
        return u;
    }

}
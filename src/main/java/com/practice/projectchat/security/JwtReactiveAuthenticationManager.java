package com.practice.projectchat.security;

import com.practice.projectchat.config.JwtProvider;
import com.practice.projectchat.domain.User;
import com.practice.projectchat.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

// AuthenticationWebFilter가 JwtPreAuthToken을 들고 authManager.authenticate(auth)를 호출. 즉, 여기서 인자로 쓰는 Authentication이 JwtPreAuthToken임
@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    // Authentication을 반환하면, AuthenticationWebFilter가 SecurityContext에 저장
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        final Jws<Claims> jws;

        try {
            jws = jwtProvider.parse(token); // 서명, 만료 검증
        } catch (ExpiredJwtException e) {
            // 만료: 프론트에서 로그인 화면 유도
            return Mono.error(new BadCredentialsException("TOKEN_EXPIRED"));
        } catch (JwtException e){
            // 기타 무효, 서명불일치
            return Mono.error(new BadCredentialsException("INVALID_TOKEN"));
        }

        Long userId = Long.valueOf(jws.getPayload().getSubject());
        String role = (String) jws.getPayload().get("role");

        return userRepository.findById(userId)
                .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
                // UsernamePasswordAuthenticationToken: 인증이 끝난 사용자 정보 담을때도 사용함
                .map(u -> new UsernamePasswordAuthenticationToken(
                        String.valueOf(u.getId()), // principal = userId
                        null, // credentials 없음
                        // SimpleGrantedAuthority: 내부에 문자열 하나만 들고 있음
                        List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
                ));
    }
}


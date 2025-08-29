package com.practice.projectchat.config;

import com.practice.projectchat.security.BearerTokenServerAuthenticationConverter;
import com.practice.projectchat.security.JsonAuthEntryPoint;
import com.practice.projectchat.security.JwtReactiveAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

/**
 * 게이트 1: 인증(Authentication) 단계
 * - AuthenticationWebFilter가 여기서 동작
 * - 토큰 추출/검증/사용자 로딩
 * - 실패 = 인증 시도 실패
 *
 * 게이트 2: 인가(Authorization) 단계
 * - authorizezExchange 규칙 검사
 * - 권한 부족 시 실패 = 접근 거부
 * - 토큰이 없으면 인증시도 자체를 하지않고 게이트2에 도착 (Authorization 헤더가 없거나 Bearer가 아니면 Mono.empty()반환)
 *
 * 실패 케이스
 * 1. 인증 시도 실패(만료/무효 토큰) -> 게이트 1에서 터짐
 * 2. 인증 없음(토큰 자체가 없거나 Bearer가 아님) -> 게이트 2에서 터짐
 * 3. 접근 거부(인증은 됐지만 권한 부족) -> 게이트 2에서 터짐
 *
 * .exceptedHandling()부분이 케이스 2번 담당
 * jwtFilter.setAuthenticationFailureHandler() 부분이 케이스 1번 담당
 * accessDeniedHandler()가 케이스 3번 담당인데 USER만 만들어줄거라 일단 추가하진 않음
 *
 */
@Configuration
public class SecurityConfig {

    // HTTP 요청 ---BearerTokenServerAuthenticationConverter--> Authentication 객체인 JwtPreAuthToken 반환
    // JwtPreAuthToken ---JwtReactiveAuthenticationManager---> 인증완료 객체인 UsernamePasswordAuthenticationToken 반환
    // 인증완료된 Authentication은 Reactor Context에 저장됨 (Session 저장과는 다름)
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            JwtReactiveAuthenticationManager authManager,
                                                            BearerTokenServerAuthenticationConverter converter,
                                                            JsonAuthEntryPoint entryPoint){
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authManager);
        jwtFilter.setServerAuthenticationConverter(converter);
        jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance()); // 인증상태를 세션에 저장하지 않음

        // 필터가 동작할 경로를 제한해서 permitAll 구간에서의 불필요한 인증시도를 차단
        ServerWebExchangeMatcher protectedApi = new AndServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers("/api/v1/**"),
                new NegatedServerWebExchangeMatcher(
                        ServerWebExchangeMatchers.pathMatchers("/api/v1/auth/**")
                )
        );
        jwtFilter.setRequiresAuthenticationMatcher(protectedApi);

        jwtFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(entryPoint));

        return http
                // 전역 보안 설정
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 쿠키사용 안해서 CSRF 비활성화
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // 세션 비활성화
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // 인가 규칙
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .anyExchange().authenticated()
                )

                // 전역 예외 처리
                // 인증이 전혀 없는 상태로 보호 경로 접근 시(인가 단계) -> EntryPoint로 401
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        // (선택) 권한 부족 403도 JSON으로 통일
                        .accessDeniedHandler((exchange, denied) -> {
                            var res = exchange.getResponse();
                            res.setStatusCode(HttpStatus.FORBIDDEN);
                            res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            var body = "{\"code\":\"FORBIDDEN\",\"message\":\"Access denied.\"}"
                                    .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            var buf = res.bufferFactory().wrap(body);
                            return res.writeWith(Mono.just(buf));
                        })
                )

                // 필터 체인에 JWT 필터 삽입 (인증 단계 위치)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}

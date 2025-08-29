package com.practice.projectchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@Configuration
@EnableR2dbcAuditing
public class R2dbcAuditingConfig {

    // 누가 저장/수정했는가
    @Bean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        // Reactive SecurityContext에서 로그인 사용자를 꺼내 loginId(또는 username)을 반환, 인증없으면 "system"으로 기록
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth != null && auth.isAuthenticated())
                .map(auth -> auth.getName()) // 사용자 식별자 (username/userId 등)
                .switchIfEmpty(Mono.just("system")); // 비로그인/배치 등
    }

    // 언제 저장/수정했는가
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(Instant.now());
    }

}

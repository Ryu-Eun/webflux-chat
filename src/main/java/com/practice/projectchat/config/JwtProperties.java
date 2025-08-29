package com.practice.projectchat.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt") // application.yml의 app.jwt.* 값을 자바 클래스 필드에 타입 안전하게 바인딩
public class JwtProperties {

    @Size(min = 32)
    private String secret;

    @NotBlank
    private String issuer;

    private Duration accessTtl = Duration.ofMinutes(60); // 기본값 설정
}

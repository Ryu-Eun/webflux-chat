package com.practice.projectchat.config;

import com.practice.projectchat.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties props;
    private final SecretKey secretKey;

    // AccessToken 발급
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTtl());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("loginId", user.getLoginId())
                .claim("nickname", user.getNickname())
                .claim("role", "USER")
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // token -> Claims
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public Instant getExpiresAt(String token) {
        return parse(token).getPayload().getExpiration().toInstant();
    }

}
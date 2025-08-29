package com.practice.projectchat.security;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// authenticated 경로에서 인증 실패 발생시 EntryPoint 진입
@Component
public class JsonAuthEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse res = exchange.getResponse();
        res.setStatusCode(HttpStatus.UNAUTHORIZED); // 401
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String code = "UNAUTHORIZED";
        String message = "Authentication required";

        if(ex instanceof BadCredentialsException bce){
            String m = bce.getMessage();
            if("TOKEN_EXPIRED".equals(m)){
                code = "TOKEN_EXPIRED";
                message = "Access token has expired";
                res.getHeaders().add(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer error=\"invalid_token\", error_description=\"expired\""
                );
            }else if("INVALID_TOKEN".equals(m)){
                code = "INVALID_TOKEN";
                message = "Invalid access token";
                res.getHeaders().add(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer error=\"invalid_token\", error_description=\"invalid\""
                );
            }
        }

        byte[] body = ("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = res.bufferFactory().wrap(body);
        return res.writeWith(Mono.just(buffer));
    }
}
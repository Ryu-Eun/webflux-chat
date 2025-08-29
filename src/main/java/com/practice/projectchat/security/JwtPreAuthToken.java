package com.practice.projectchat.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class JwtPreAuthToken extends AbstractAuthenticationToken {

    private final String token;

    JwtPreAuthToken(String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        setAuthenticated(false);
    }

    // Credentials: 인증을 증명하기 위해 제출하는 값
    @Override
    public Object getCredentials() {
        return token;
    }

    // Principal: 누가 인증되었는지 나타내는 식별자
    @Override
    public Object getPrincipal() {
        return null;
    }
}

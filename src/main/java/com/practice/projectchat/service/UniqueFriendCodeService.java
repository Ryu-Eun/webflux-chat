package com.practice.projectchat.service;

import com.practice.projectchat.repository.UserRepository;
import com.practice.projectchat.util.FriendCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UniqueFriendCodeService {

    private static final int MAX_ATTEMPTS = 5;

    private final FriendCodeGenerator generator;
    private final UserRepository userRepository;

    public Mono<String> generateUnique(){
        return tryOnce(1);
    }

    private Mono<String> tryOnce(int attempt) {
        String code = generator.generateFriendCode();
        return userRepository.existsByFriendCode(code)
                .flatMap(exists -> {
                    if(!exists) return Mono.just(code);
                    if(attempt >= MAX_ATTEMPTS){ // MAX_ATTEMPTS를 넘어갔을 때
                        return Mono.error(new IllegalStateException("친구코드 생성 충돌이 발생했습니다."));
                    }
                    return tryOnce(attempt + 1);
                });
    }

}
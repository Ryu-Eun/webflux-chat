package com.practice.projectchat.service;

import com.practice.projectchat.domain.User;
import com.practice.projectchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // userId로 유저 정보 조회
    public Mono<User> getById(Long userId){
        return userRepository.findById(userId);
    }

}
package com.practice.projectchat.repository;

import com.practice.projectchat.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<Boolean> existsByLoginId(String loginId);
    Mono<Boolean> existsByFriendCode(String friendCode);
    Mono<User> findByLoginId(String loginId);
}

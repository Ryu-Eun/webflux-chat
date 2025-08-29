package com.practice.projectchat.controller;

import com.practice.projectchat.dto.UserDto;
import com.practice.projectchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<ResponseEntity<UserDto.MeResponse>> me(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        final Long userId;
        try {
            userId = Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        return userService.getMe(userId)
                .map(UserDto.MeResponse::toMeResponse) // 엔티티 → DTO 매핑
                .map(ResponseEntity::ok)// 200
                .defaultIfEmpty(ResponseEntity.notFound().build()); // 없으면 404

    }

}
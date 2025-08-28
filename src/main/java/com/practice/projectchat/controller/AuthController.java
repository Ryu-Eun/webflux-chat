package com.practice.projectchat.controller;

import com.practice.projectchat.dto.UserDto;
import com.practice.projectchat.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<UserDto.SignupResponse>> signup(@Valid @RequestBody UserDto.SignupRequest request){
        var command = new UserAuthService.SignupCommand(
                request.getLoginId().trim().toLowerCase(Locale.ROOT),
                request.getPassword(),
                request.getNickname().trim()
        );
        return userAuthService.createUser(command)
                .map(r -> new UserDto.SignupResponse(r.userId(), r.nickname(), r.friendCode()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body)); // 201
    }

}

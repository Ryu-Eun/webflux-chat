package com.practice.projectchat.controller;

import com.practice.projectchat.dto.FriendRequestDto;
import com.practice.projectchat.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/friends/requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    // 친구 요청 보내기
    @PostMapping("/send")
    public Mono<ResponseEntity<FriendRequestDto.RequestInfo>> sendRequest(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody FriendRequestDto.SendRequest request
    ){
        Long requesterId = Long.valueOf(userIdStr);
        return friendRequestService.sendRequest(requesterId, request.getReceiverId())
                .map(FriendRequestDto.RequestInfo::fromEntity)
                .map(ResponseEntity::ok);
    }

    // 받은 요청 목록 조회
    @GetMapping("/received")
    public Mono<ResponseEntity<Flux<FriendRequestDto.RequestInfo>>> getReceivedRequests(
            @AuthenticationPrincipal String userIdStr
    ) {
        Long userId = Long.valueOf(userIdStr);
        Flux<FriendRequestDto.RequestInfo> body = friendRequestService.getReceivedRequests(userId)
                .map(FriendRequestDto.RequestInfo::fromEntity);

        return Mono.just(ResponseEntity.ok(body));
    }

}
package com.practice.projectchat.controller;

import com.practice.projectchat.domain.FriendShip;
import com.practice.projectchat.dto.FriendShipDto;
import com.practice.projectchat.service.FriendShipService;
import com.practice.projectchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendShipController {

    private final FriendShipService friendShipService;
    private final UserService userService;

    // 친구 목록 조회
    @GetMapping
    public Mono<ResponseEntity<Flux<FriendShipDto.FriendInfo>>> getFriends(@AuthenticationPrincipal String userIdStr) {
        Long userId = Long.valueOf(userIdStr);
        Flux<FriendShipDto.FriendInfo> body = friendShipService.getFriends(userId)
                .flatMap(fs -> userService.getById(fs.getFriendUserId())
                        .map(u -> FriendShipDto.FriendInfo.from(fs, u.getNickname()))
                );
        return Mono.just(ResponseEntity.ok(body));
    }

    // 친구 삭제 (양방향)
    @DeleteMapping("/{friendId}")
    public Mono<ResponseEntity<FriendShipDto.FriendDeleteResponse>> deleteFriend(@AuthenticationPrincipal String userIdStr,
                                                                                 @PathVariable Long friendId) {
        Long userId = Long.valueOf(userIdStr);
        return friendShipService.deleteFriend(userId, friendId)
                .then(Mono.just(ResponseEntity.ok(
                        FriendShipDto.FriendDeleteResponse.builder()
                                .friendId(friendId)
                                .message("친구 관계가 삭제되었습니다.")
                                .build()
                )));
    }

    // 친구 여부 확인
    @GetMapping("/{friendId}/exists")
    public Mono<ResponseEntity<Boolean>> areFriends(@AuthenticationPrincipal String userIdStr,
                                                    @PathVariable Long friendId) {
        Long userId = Long.valueOf(userIdStr);
        return friendShipService.areFriends(userId, friendId)
                .map(ResponseEntity::ok);
    }

    // 친구 관계 상세 조회
    @GetMapping("/{friendId}/detail")
    public Mono<ResponseEntity<FriendShip>> getFriendship(@AuthenticationPrincipal String userIdStr,
                                                          @PathVariable Long friendId) {
        Long userId = Long.valueOf(userIdStr);
        return friendShipService.getFriendShip(userId, friendId)
                .map(ResponseEntity::ok);
    }

}
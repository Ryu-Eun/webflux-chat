package com.practice.projectchat.controller;

import com.practice.projectchat.domain.ChatRoom;
import com.practice.projectchat.dto.ChatRoomDto;
import com.practice.projectchat.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 내 채팅방 목록 (최근 대화 순)
    @GetMapping
    public Mono<ResponseEntity<Flux<ChatRoomDto.RoomListItem>>> getMyRooms(
            @AuthenticationPrincipal String userIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long me = Long.valueOf(userIdStr);
        Flux<ChatRoomDto.RoomListItem> body = chatRoomService.getMyRooms(me, page, size);
        return Mono.just(ResponseEntity.ok(body));
    }

    // PRIVATE 방 생성
    @PostMapping("/private")
    public Mono<ResponseEntity<ChatRoomDto.RoomCreatedResponse>> createPrivate(
            @AuthenticationPrincipal String userIdStr,
            @Valid @RequestBody ChatRoomDto.CreatePrivateRequest req
    ) {
        Long me = Long.valueOf(userIdStr);
        return chatRoomService.createPrivateRoom(me, req.getFriendId())
                .map(r -> ChatRoomDto.RoomCreatedResponse.builder()
                        .roomId(r.getId())
                        .type(r.getType().name())
                        .name(r.getType() == ChatRoom.ChatRoomType.GROUP ? r.getName() : null)
                        .build())
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    // GROUP 방 생성
    @PostMapping("/group")
    public Mono<ResponseEntity<ChatRoomDto.RoomCreatedResponse>> createGroup(
            @AuthenticationPrincipal String userIdStr,
            @Valid @RequestBody ChatRoomDto.CreateGroupRequest req
    ) {
        Long me = Long.valueOf(userIdStr);
        return chatRoomService.createGroupRoom(me, req.getMemberIds())
                .map(r -> ChatRoomDto.RoomCreatedResponse.builder()
                        .roomId(r.getId())
                        .type(r.getType().name())
                        .name(r.getName())
                        .build())
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    // 멤버 초대 (PRIVATE/ＧROUP 공통)
    @PostMapping("/{roomId}/invite")
    public Mono<ResponseEntity<ChatRoomDto.InviteResultResponse>> invite(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatRoomDto.InviteRequest req
    ) {
        Long me = Long.valueOf(userIdStr);
        return chatRoomService.inviteMembers(roomId, me, req.getUserIds())
                .map(res -> ChatRoomDto.InviteResultResponse.builder()
                        .outcome(res.getOutcome().name())
                        .roomId(res.getRoomId())
                        .addedCount(res.getAddedCount())
                        .reactivatedCount(res.getReactivatedCount())
                        .skippedAlreadyActive(res.getSkippedAlreadyActive())
                        .build())
                .map(ResponseEntity::ok);
    }

    // 나가기
    @PostMapping("/{roomId}/leave")
    public Mono<ResponseEntity<Void>> leave(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long roomId
    ) {
        Long me = Long.valueOf(userIdStr);
        return chatRoomService.leaveRoom(roomId, me)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

}
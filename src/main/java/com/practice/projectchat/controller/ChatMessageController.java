package com.practice.projectchat.controller;

import com.practice.projectchat.dto.MessageDto;
import com.practice.projectchat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@RequestMapping("/api/v1/chat/rooms/{roomId}/messages")
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 전송
    @PostMapping
    public Mono<ResponseEntity<MessageDto.MessageResponse>> send(
            @AuthenticationPrincipal String userId,
            @PathVariable Long roomId,
            @Valid @RequestBody MessageDto.SendRequest req
    ){
        Long me = Long.valueOf(userId);
        return chatMessageService.sendChatMessage(roomId, me, req)
                .map(ResponseEntity::ok);
    }

    // 조회
    @GetMapping
    public Mono<ResponseEntity<Flux<MessageDto.MessageResponse>>> list(
            @AuthenticationPrincipal String userId,
            @PathVariable Long roomId,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "before", required = false) String before
    ){
        Long me = Long.valueOf(userId);
        int safe = Math.min(Math.max(limit, 1), 100);

        if(before == null || before.isBlank()){
            Flux<MessageDto.MessageResponse> body = chatMessageService.getLatestChatMessages(roomId, me, safe);
            return Mono.just(ResponseEntity.ok(body));
        }else{
            final Instant beforeTs;
            try{
                beforeTs = Instant.parse(before);
            }catch(DateTimeParseException e){
                return Mono.just(ResponseEntity.badRequest().build()); // 400
            }
            Flux<MessageDto.MessageResponse> body = chatMessageService.getBeforeChatMessages(roomId, me, beforeTs, safe);
            return Mono.just(ResponseEntity.ok(body));
        }
    }
}
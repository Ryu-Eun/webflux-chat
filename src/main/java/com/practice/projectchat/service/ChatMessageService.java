package com.practice.projectchat.service;

import com.practice.projectchat.domain.ChatMessage;
import com.practice.projectchat.dto.MessageDto;
import com.practice.projectchat.repository.ChatMessageRepository;
import com.practice.projectchat.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    // 메시지 전송
    @Transactional
    public Mono<MessageDto.MessageResponse> sendChatMessage(Long roomId, Long senderId, MessageDto.SendRequest req){
        return chatRoomMemberRepository.existsByRoomIdAndUserIdAndIsActiveTrue(roomId, senderId)
                .flatMap(exists -> {
                    if(!exists){
                        return Mono.error(new IllegalStateException("해당 방의 멤버가 아닙니다."));
                    }

                    ChatMessage message = new ChatMessage();
                    message.setRoomId(roomId);
                    message.setSenderId(senderId);
                    message.setType(ChatMessage.MessageType.valueOf(req.getType())); // TEXT 타입
                    message.setCreatedAt(Instant.now());
                    String trimmed = req.getContent().trim();
                    if(trimmed.isEmpty()){
                        return Mono.error(new IllegalStateException("메시지 내용이 비어있습니다."));
                    }
                    message.setContent(trimmed);

                    return chatMessageRepository.save(message)
                            .map(MessageDto.MessageResponse::fromEntity);
                });
    }

    // 최신 N개
    public Flux<MessageDto.MessageResponse> getLatestChatMessages(Long roomId, Long userId, int limit){
        return ensureMember(roomId,userId)
                .thenMany(chatMessageRepository.findLatestMessages(roomId, limit))
                .map(MessageDto.MessageResponse::fromEntity);
    }

    // 무한 스크롤: 기준시각(before) 이전 N개
    public Flux<MessageDto.MessageResponse> getBeforeChatMessages(Long roomId, Long userId, Instant before, int limit){
        return ensureMember(roomId, userId)
                .thenMany(chatMessageRepository.findMessagesBefore(roomId, before, limit))
                .map(MessageDto.MessageResponse::fromEntity);
    }


    private Mono<Boolean> ensureMember(Long roomId, Long userId){
        return chatRoomMemberRepository.existsByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
                .flatMap(exists -> exists ? Mono.just(true)
                        : Mono.error(new IllegalStateException("해당 방의 멤버가 아닙니다.")));
    }

}
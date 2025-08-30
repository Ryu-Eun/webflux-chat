package com.practice.projectchat.repository;

import com.practice.projectchat.domain.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

    // 특정 방의 메시지 조회 (최신순으로 페이징)
    // Spring Data R2DBC에서는 페이징을 지원하지 않는다.
    // 최신 메시지 N개 (처음 진입 시)
    @Query("SELECT * FROM chat_messages " +
            "WHERE room_id = :roomId " +
            "ORDER BY created_at DESC " +
            "LIMIT :limit")
    Flux<ChatMessage> findLatestMessages(Long roomId, int limit);

    // 무한 스크롤 (기준 시각 이전 메시지 조회)
    @Query("SELECT * FROM chat_messages " +
            "WHERE room_id = :roomId AND created_at < :beforeTime " +
            "ORDER BY created_at DESC " +
            "LIMIT :limit")
    Flux<ChatMessage> findMessagesBefore(Long roomId, Instant beforeTime, int limit);

    // 채팅방 목록에서 해당 방의 최근 메시지 1개 미리보기
    @Query("SELECT * FROM chat_messages " +
            "WHERE room_id = :roomId " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Mono<ChatMessage> findLastMessageByRoomId(Long roomId);

}

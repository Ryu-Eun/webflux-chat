package com.practice.projectchat.repository;

import com.practice.projectchat.domain.ChatRoom;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {

    // 1:1 채팅방 중에서, 두 유저가 아직 방에 남아있어야함
    // 만약 1:1 채팅방 상태에서 한쪽이 나간 상태라면, 남은 한쪽은 해당 채팅방에 그대로 남아있고, 그 사용자가 다시 나갔던 사용자를 다시 초대해도 새 1:1 채팅방이 생성된다
    // 그리고 만약 나갔던 사용자가 그 채팅방에 남아있던 사용자에게 메시지를 다시보낸다고 해도 1:1 채팅방이 새로 생성된다. 즉, 기록 이어지지 않음
    @Query("""
        SELECT r.* 
        FROM chat_rooms r
        JOIN chat_room_members m ON r.id = m.room_id
        WHERE r.type = 'PRIVATE' 
          AND m.is_active = TRUE
          AND m.user_id IN (:userId1, :userId2)
        GROUP BY r.id
        HAVING COUNT(DISTINCT m.user_id) = 2
        LIMIT 1
    """)
    Mono<ChatRoom> findPrivateRoomBetweenUsers(Long userId1, Long userId2);

}

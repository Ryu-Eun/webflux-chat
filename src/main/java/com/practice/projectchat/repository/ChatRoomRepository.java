package com.practice.projectchat.repository;

import com.practice.projectchat.domain.ChatRoom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {

}

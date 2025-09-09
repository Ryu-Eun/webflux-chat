package com.practice.projectchat.repository;

import com.practice.projectchat.domain.ChatRoomMember;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRoomMemberRepository extends ReactiveCrudRepository<ChatRoomMember, Long> {

    // 해당 방에 속한 멤버들 조회
    Flux<ChatRoomMember> findByRoomIdAndIsActiveTrue(Long roomId);

    // 내가 참여 중인 방 목록
    Flux<ChatRoomMember> findByUserIdAndIsActiveTrue(Long userId);

    // 특정 유저가 방에 참여 중인지 여부
    // 대화방에서 채팅을 보낼때, 내가 있는 대화방에 누군가를 초대할때, 대화방 나갈때 등 참여중인지 여부 따져야됨
    Mono<Boolean> existsByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userID);

    // 특정 방에서 특정 유지 조회
    Mono<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    // private 방에서 상대가 나갔어도 이름을 보여주려면, 활성/비활성 구분없이 방의 구성 멤버를 볼 수 있어야함
    Flux<ChatRoomMember> findByRoomId(Long roomId);

}

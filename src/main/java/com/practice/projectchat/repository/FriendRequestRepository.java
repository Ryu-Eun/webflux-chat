package com.practice.projectchat.repository;

import com.practice.projectchat.domain.FriendRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FriendRequestRepository extends ReactiveCrudRepository<FriendRequest, Long> {

    // 요청이 이미 존재하는지 (중복 방지)
    Mono<Boolean> existsByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    // 내가 보낸 요청들 (PENDING)
    Flux<FriendRequest> findByRequesterIdAndStatus(Long requesterId, FriendRequest.RequestStatus stats);

    // 내가 받은 요청들 (PENDING)
    Flux<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequest.RequestStatus stats);

    // 특정 요청 조회 -> 이후 status update로 수락/거절
    Mono<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

}
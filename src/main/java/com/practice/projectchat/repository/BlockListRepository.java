package com.practice.projectchat.repository;

import com.practice.projectchat.domain.BlockList;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BlockListRepository extends ReactiveCrudRepository<BlockList, Long> {

    // 특정 유저가 다른 유저를 차단한 상태인지 여부
    Mono<Boolean> existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    // 내가 차단한 모든 사용자 목록 조회
    Flux<BlockList> findByUserId(Long userId);


}

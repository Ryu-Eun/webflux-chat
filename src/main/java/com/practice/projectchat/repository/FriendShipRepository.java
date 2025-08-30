package com.practice.projectchat.repository;

import com.practice.projectchat.domain.FriendShip;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FriendShipRepository extends ReactiveCrudRepository<FriendShip, String> {

    // 두 유저가 서로 친구인지 확인 (ACTIVE 상태)
    // 사실 한쪽에서 친구인지만 확인하면 반대쪽에선 확인 안해줘도 되지만 데이터 정합성이 혹시 틀어져있을 수 있어서 양쪽 검사
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM friendships " +
            "WHERE ((user_id = :userId AND friend_user_id = :friendId) " +
            "   OR (user_id = :friendId AND friend_user_id = :userId)) " +
            "AND status = 'ACTIVE'")
    Mono<Boolean> areFriends(Long userId, Long friendId);

    // 내 친구 목록 조회
    Flux<FriendShip> findByUserIdAndStatus(Long userId, FriendShip.FriendshipStatus status);

    // 내 관점에서 특정 친구 정보 가져오기
    Mono<FriendShip> findByUserIdAndFriendUserId(Long userId, Long friendUserId);

    // 친구 삭제 (양방향 모두 상태 변경되어야함)
    @Query("UPDATE friendships " +
            "SET status = 'DELETED', updated_at = now() " +
            "WHERE (user_id = :userId AND friend_user_id = :friendId) " +
            "   OR (user_id = :friendId AND friend_user_id = :userId) " +
            "RETURNING *") // RETURNING: PostgreSQL에서 UPDATE+SELECT 합친 것처럼 동작함. 원래 UPDATE만 쓰면 몇개의 row가 수정됐는지만 알려줌
    Flux<FriendShip> deleteFriendship(Long userId, Long friendId);

}

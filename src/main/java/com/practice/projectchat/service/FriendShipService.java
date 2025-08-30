package com.practice.projectchat.service;

import com.practice.projectchat.domain.FriendShip;
import com.practice.projectchat.exception.AlreadyDeletedException;
import com.practice.projectchat.exception.FriendShipNotFoundException;
import com.practice.projectchat.repository.FriendShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FriendShipService {

    private final FriendShipRepository friendShipRepository;

    // 내가 등록한 모든 친구 조회 (내 기준 status = ACTIVE 만)
    public Flux<FriendShip> getFriends(Long userId){
        return friendShipRepository.findByUserIdAndStatus(userId, FriendShip.FriendshipStatus.ACTIVE);
    }

    // 친구 삭제 (양방향)
    public Mono<Void> deleteFriend(Long userId, Long friendId){
        return friendShipRepository.findByUserIdAndFriendUserId(userId, friendId)
                .switchIfEmpty(Mono.error(new FriendShipNotFoundException("친구 관계를 찾을 수 없습니다.")))
                .flatMap(fs -> {
                    if(fs.getStatus() == FriendShip.FriendshipStatus.DELETED){
                        return Mono.error(new AlreadyDeletedException("이미 삭제된 친구관계입니다."));
                    }
                    // 양방향 DELETED 업데이트
                    return friendShipRepository.deleteFriendship(userId, friendId).then();
                });
    }

    // 두 유저가 서로 친구인지 확인
    public Mono<Boolean> areFriends(Long userId, Long friendId){
        return friendShipRepository.areFriends(userId, friendId);
    }

    // 특정 친구 관계 조회
    public Mono<FriendShip> getFriendShip(Long userId, Long friendId){
        return friendShipRepository.findByUserIdAndFriendUserId(userId, friendId)
                .switchIfEmpty(Mono.error(new FriendShipNotFoundException("친구 관계를 찾을 수 없습니다.")));
    }

}

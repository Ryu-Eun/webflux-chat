package com.practice.projectchat.service;

import com.practice.projectchat.domain.FriendRequest;
import com.practice.projectchat.domain.FriendShip;
import com.practice.projectchat.exception.*;
import com.practice.projectchat.repository.FriendRequestRepository;
import com.practice.projectchat.repository.FriendShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendShipRepository friendShipRepository;

    // 친구 요청 보내기
    public Mono<FriendRequest> sendRequest(Long requesterId, Long receiverId){
        // 1. 이미 친구인지 확인
        return friendShipRepository.areFriends(requesterId, receiverId)
                .flatMap(isFriend -> {
                    if(isFriend){
                        return Mono.error(new AlreadyFriendException("이미 친구 상태입니다."));
                    }

                    // 2. 기존 요청이 있는지 확인
                    return friendRequestRepository.existsByRequesterIdAndReceiverId(requesterId, receiverId)
                            .flatMap(exists -> {
                                if(exists){
                                    return Mono.error(new DuplicateFriendRequestException("이미 친구 요청을 보냈습니다."));
                                }

                                FriendRequest req = FriendRequest.builder()
                                        .requesterId(requesterId)
                                        .receiverId(receiverId)
                                        .status(FriendRequest.RequestStatus.PENDING)
                                        .build();

                                return friendRequestRepository.save(req);
                            });
                });
    }

    // 받은 요청 목록 조회 (PENDING)
    public Flux<FriendRequest> getReceivedRequests(Long userId){
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.RequestStatus.PENDING);
    }

    // 친구요청 수락
    // Reactive에서는 React 체인 중간에 subscribe같은걸로 끊어버리면 안됨. 무조건 .flatMap(), .then() 같은 오퍼레이터로 체인을 이어야 같은 트랜잭션 안에 묶인다
    @Transactional
    public Mono<Void> acceptRequest(Long requestId, Long receiverId){
        return friendRequestRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new FriendRequestNotFoundException("요청을 찾을 수 없습니다.")))
                .flatMap(req -> {
                    if(!req.getReceiverId().equals(receiverId)){
                        return Mono.error(new FriendRequestPermissionException("이 요청을 수락할 권한이 없습니다."));
                    }
                    if(req.getStatus() != FriendRequest.RequestStatus.PENDING){
                        return Mono.error(new FriendRequestAlreadyHandledException("이미 처리된 요청입니다."));
                    }

                    // 요청 상태 업데이트 PENDING -> ACCEPTED
                    req.setStatus(FriendRequest.RequestStatus.ACCEPTED);

                    // FriendShip 2개 생성 (양방향)
                    FriendShip friendShip1 = FriendShip.builder()
                            .userId(req.getRequesterId())
                            .friendUserId(req.getReceiverId())
                            .status(FriendShip.FriendshipStatus.ACTIVE)
                            .build();

                    FriendShip friendShip2 = FriendShip.builder()
                            .userId(req.getReceiverId())
                            .friendUserId(req.getRequesterId())
                            .status(FriendShip.FriendshipStatus.ACTIVE)
                            .build();

                    return friendRequestRepository.save(req)
                            .thenMany(friendShipRepository.saveAll(List.of(friendShip1, friendShip2)))
                            .then();
                });
    }

    // 친구요청 거절
    @Transactional
    public Mono<Void> rejectRequest(Long requestId, Long receiverId){
        return friendRequestRepository.findById(requestId)
                .switchIfEmpty(Mono.error(new FriendRequestNotFoundException("요청을 찾을 수 없습니다.")))
                .flatMap(req -> {
                    if (!req.getReceiverId().equals(receiverId)) {
                        return Mono.error(new FriendRequestPermissionException("이 요청을 거절할 권한이 없습니다."));
                    }
                    if (req.getStatus() != FriendRequest.RequestStatus.PENDING) {
                        return Mono.error(new IllegalStateException("이미 처리된 요청입니다."));
                    }

                    req.setStatus(FriendRequest.RequestStatus.REJECTED);
                    return friendRequestRepository.save(req).then();
                });
    }

}
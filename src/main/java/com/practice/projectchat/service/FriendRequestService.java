package com.practice.projectchat.service;

import com.practice.projectchat.domain.FriendRequest;
import com.practice.projectchat.exception.AlreadyFriendException;
import com.practice.projectchat.exception.DuplicateFriendRequestException;
import com.practice.projectchat.repository.FriendRequestRepository;
import com.practice.projectchat.repository.FriendShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

}
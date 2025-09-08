package com.practice.projectchat.service;

import com.practice.projectchat.domain.ChatMessage;
import com.practice.projectchat.domain.ChatRoom;
import com.practice.projectchat.domain.ChatRoomMember;
import com.practice.projectchat.domain.User;
import com.practice.projectchat.dto.ChatRoomDto;
import com.practice.projectchat.dto.InviteResult;
import com.practice.projectchat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final FriendShipRepository friendShipRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;


    @Transactional
    public Mono<ChatRoom> createPrivateRoom(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return Mono.error(new IllegalStateException("자기 자신과는 대화방을 만들 수 없습니다."));
        }
        return friendShipRepository.areFriends(userId, friendId)
                .flatMap(isFriend -> {
                    if (!isFriend) {
                        return Mono.error(new IllegalStateException("친구가 아닌 유저와는 대화방을 만들 수 없습니다."));
                    }
                    return chatRoomRepository.findPrivateRoomBetweenUsers(userId, friendId)
                            .switchIfEmpty( // 없으면
                                    Mono.defer(() -> // Publisher<ChatRoom>
                                    chatRoomRepository.save(ChatRoom.builder()
                                                    .type(ChatRoom.ChatRoomType.PRIVATE)
                                                    .build()) // Mono<ChatRoom>
                                            .flatMap(room -> { // chatRoom -> Publisher<ChatRoom>
                                                var members = List.of(userId, friendId).stream().distinct()
                                                        .map(uid -> ChatRoomMember.builder()
                                                                .roomId(room.getId())
                                                                .userId(uid)
                                                                .isActive(true)
                                                                .build())
                                                        .toList();
                                                // Flux<ChatRoomMember> 완료되면 room만 반환
                                                return chatRoomMemberRepository.saveAll(members)
                                                        .then(Mono.just(room)); // Mono<ChatRoom>
                                            })
                            ));
                });
    }


    // TODO: block_list 고려 시, 초대 불가한 사용자 필터링
    // TODO: SYSTEM 메시지 "그룹방이 생성되었습니다" 남기기
    // Group 채팅방 생성
    @Transactional
    public Mono<ChatRoom> createGroupRoom(Long creatorId, List<Long> memberIds){ // memberIds에는 creatorId가 포함 안됨
        var baseStream = (memberIds == null ? Stream.<Long>empty() : memberIds.stream()); // null 방지
        List<Long> allMemberIds = Stream.concat(Stream.of(creatorId), baseStream) // 생성자 + 초대 멤버들을 하나로 모으고 중복제거
                .distinct()
                .toList();

        // 1. 인원수 검증 (3명이상 50명이하)
        if(allMemberIds.size() < 3){
            return Mono.error(new IllegalArgumentException("그룹 채팅방은 최소 3명 이상이어야 합니다."));
        }
        if(allMemberIds.size() > ChatRoom.MAX_MEMBERS){
            return Mono.error(new IllegalStateException("그룹 채팅방 최대 인원("+ ChatRoom.MAX_MEMBERS + ")을 초과했습니다."));
        }

        // 2. 존재하는 사용자들만 모으기
        return userRepository.findAllById(allMemberIds)
                .collectList() // Mono<List<User>>
                .flatMap(users -> {
                    if(users.size() != allMemberIds.size()){ // 개수 다르면 일부 ID가 없음
                        // 존재하지 않는 사용자 ID 찾아서 에러
                        var found = users.stream().map(User::getId).collect(Collectors.toSet());
                        var missing = allMemberIds.stream().filter(id -> !found.contains(id)).toList();
                        return Mono.error(new IllegalStateException("존재하지 않는 사용자: " + missing));
                    }

                    // 3. 그룹명 생성
                    var creatorOpt = users.stream().filter(u -> u.getId().equals(creatorId)).findFirst();
                    var others = users.stream().filter(u -> !u.getId().equals(creatorId))
                            .sorted(Comparator.comparing(User::getNickname))
                            .toList();

                    List<User> ordered = new ArrayList<>(users.size());
                    creatorOpt.ifPresent(ordered::add); // creator 먼저
                    ordered.addAll(others); // 나머지 멤버들

                    String roomName = buildGroupName(ordered);

                    // 4. 방 생성 -> 저장
                    ChatRoom room = ChatRoom.builder()
                            .type(ChatRoom.ChatRoomType.GROUP)
                            .name(roomName)
                            .build();

                    return chatRoomRepository.save(room) // Mono<ChatRoom>
                            .flatMap(saved -> {
                                var members = allMemberIds.stream()
                                        .map(uid -> ChatRoomMember.builder()
                                                .roomId(saved.getId())
                                                .userId(uid)
                                                .isActive(true)
                                                .build())
                                        .toList();

                                return chatRoomMemberRepository.saveAll(members) // Flux<ChatRoomMember>
                                        .then(Mono.just(saved)); // Mono<ChatRoom>
                            });
                });
    }


    // TODO: block_list 적용
    // TODO: SYSTEM 메시지 "그룹방이 생성되었습니다"
    // 1:1채팅방 +n명 추가 -> GROUP채팅방으로 새로 만들어짐
    @Transactional
    public Mono<ChatRoom> createGroupRoomFromPrivate(Long privateRoomId, Long inviterId, List<Long> newMemberIds) {
        if (newMemberIds == null || newMemberIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException("추가할 멤버가 1명 이상 필요합니다."));
        }

        return chatRoomRepository.findById(privateRoomId)
                .switchIfEmpty(Mono.error(new IllegalStateException("채팅방을 찾을 수 없습니다.")))
                .flatMap(room -> {
                    if (room.getType() != ChatRoom.ChatRoomType.PRIVATE) {
                        return Mono.error(new IllegalStateException("PRIVATE 방이 아닙니다."));
                    }
                    return chatRoomMemberRepository.findByRoomIdAndIsActiveTrue(privateRoomId)
                            .map(ChatRoomMember::getUserId)
                            .collectList()
                            .flatMap(active -> {
                                if (!active.contains(inviterId)) {
                                    return Mono.error(new IllegalStateException("초대 권한이 없습니다."));
                                }

                                // === 분기 ===
                                if (active.size() == 1) {
                                    // (A) 한 명만 남아있다
                                    var distinctNew = newMemberIds.stream().distinct().toList();
                                    if (distinctNew.size() == 1) {
                                        // 1명 초대 → 새 PRIVATE 생성 (친구 여부는 createPrivateRoom 내부에서 검증됨)
                                        Long target = distinctNew.get(0);
                                        return createPrivateRoom(inviterId, target);
                                    }
                                    // 2명 이상 초대 → 새 GROUP 생성
                                    List<Long> candidate = new ArrayList<>();
                                    candidate.add(inviterId);
                                    candidate.addAll(distinctNew);
                                    return createGroupFromMembers(inviterId, candidate);
                                } else if (active.size() == 2) {
                                    // (B) 두 명 다 활성 → 기존 2명 + 초대자들 = GROUP
                                    List<Long> candidate = new ArrayList<>(active);
                                    candidate.addAll(newMemberIds);
                                    candidate = candidate.stream().distinct().toList();
                                    return createGroupFromMembers(inviterId, candidate);
                                } else {
                                    // 예상치 못한 상태(데이터 정합성 문제)
                                    return Mono.error(new IllegalStateException("PRIVATE 방의 활성 멤버 수가 올바르지 않습니다."));
                                }
                            });
                });
    }


    // 공통 그룹 생성 로직(검증 + 저장)
    private Mono<ChatRoom> createGroupFromMembers(Long firstUserId, List<Long> memberIdsDistinct) {
        if (memberIdsDistinct.size() < 3) {
            return Mono.error(new IllegalArgumentException("그룹 채팅방은 최소 3명 이상이어야 합니다."));
        }
        if (memberIdsDistinct.size() > ChatRoom.MAX_MEMBERS) {
            return Mono.error(new IllegalStateException("그룹 채팅방 최대 인원(" + ChatRoom.MAX_MEMBERS + ")을 초과했습니다."));
        }
        return userRepository.findAllById(memberIdsDistinct)
                .collectList()
                .flatMap(users -> {
                    if (users.size() != memberIdsDistinct.size()) {
                        var found = users.stream().map(User::getId).collect(Collectors.toSet());
                        var missing = memberIdsDistinct.stream().filter(id -> !found.contains(id)).toList();
                        return Mono.error(new IllegalStateException("존재하지 않는 사용자: " + missing));
                    }
                    // 이름: firstUserId 맨 앞 → 나머지 닉네임 오름차순
                    List<User> ordered = orderMembersWithFirst(firstUserId, users);
                    String roomName = buildGroupName(ordered);

                    ChatRoom room = ChatRoom.builder()
                            .type(ChatRoom.ChatRoomType.GROUP)
                            .name(roomName)
                            .build();

                    return chatRoomRepository.save(room)
                            .flatMap(saved -> {
                                var members = memberIdsDistinct.stream()
                                        .map(uid -> ChatRoomMember.builder()
                                                .roomId(saved.getId())
                                                .userId(uid)
                                                .isActive(true)
                                                .build())
                                        .toList();
                                return chatRoomMemberRepository.saveAll(members)
                                        .then(Mono.just(saved));
                            });
                });
    }


    // TODO: SYSTEM 메시지 "유저 X가 나갔습니다" 남기기 (system유저로)
    // TODO: block_list 관련
    // 나가기 (row삭제가 아닌, is_active=false 처리)
    @Transactional
    public Mono<Void> leaveRoom(Long roomId, Long userId){
        return chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .switchIfEmpty(Mono.error(new IllegalStateException("해당 방의 멤버가 아닙니다.")))
                .flatMap(m -> {
                    // 이미 나간 상태인지 확인
                    if(Boolean.FALSE.equals(m.getIsActive())){
                        return Mono.empty();
                    }
                    m.setIsActive(false);
                    m.setLeftAt(Instant.now());

                    return chatRoomMemberRepository.save(m).then();
                });
    }


    // 내 채팅방 목록 조회 (최근 새 채팅이 있었던 채팅방순 + 방 생성 순으로, is_active=true인 채팅방)
    // 미리보기로 최근 메시지 1개. 표시이름은 PRIVATE은 상대 닉네임, GROUP은 chat_rooms의 name컬럼
    public Flux<ChatRoomDto.RoomListItem> getMyRooms(Long me, int page, int size) {
        final int pageSafe = Math.max(page, 0);
        final int sizeSafe = Math.min(Math.max(size, 1), 100); // size 1~100 가드

        return chatRoomMemberRepository.findByUserIdAndIsActiveTrue(me)
                .map(ChatRoomMember::getRoomId)
                .distinct()
                .flatMap(buildListItemForRoom(me))  // roomId -> Mono<RoomListItem>
                .collectList()
                .map(list -> {
                    // 최근 대화순 정렬 (null 방어: lastMessageAt 없으면 roomCreatedAt)
                    list.sort((a, b) -> {
                        var aAt = a.getLastMessageAt();
                        var bAt = b.getLastMessageAt();
                        long aEpoch = (aAt == null ? 0L : aAt.toEpochMilli());
                        long bEpoch = (bAt == null ? 0L : bAt.toEpochMilli());
                        return Long.compare(bEpoch, aEpoch); // DESC
                    });

                    int from = Math.min(pageSafe * sizeSafe, list.size());
                    int to = Math.min(from + sizeSafe, list.size());
                    return list.subList(from, to);
                })
                .flatMapMany(Flux::fromIterable);
    }


    // 멤버 초대 (초대자는 해당방의 isActive=true 상태여야함)
    // PRIVATE 채팅방: 활성멤버 1명 + 초대 1명 -> 새 PRIVATE 생성
    //               그외(1명+2명추가, 2명+1명추가 등 3명이 넘어가면) -> 새 GROUP 생성
    // GROUP 채팅방: 기존 방에 신규 추가, 나갔던 멤버 다시 초대한 거면 재활성화
    @Transactional
    public Mono<InviteResult> inviteMembers(Long roomId, Long inviterId, List<Long> newMemberIds){
        if (newMemberIds == null || newMemberIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException("추가할 멤버가 1명 이상 필요합니다."));
        }
        //자기 자신 초대 방지 + 중복 제거
        final List<Long> distinctTargets = newMemberIds.stream()
                .filter(id -> !id.equals(inviterId))
                .distinct()
                .toList();
        if (distinctTargets.isEmpty()) {
            return Mono.error(new IllegalArgumentException("유효한 초대 대상이 없습니다."));
        }

        return chatRoomRepository.findById(roomId)
                .switchIfEmpty(Mono.error(new IllegalStateException("채팅방을 찾을 수 없습니다.")))
                .flatMap(room ->
                        // 초대자가 활성 멤버인지 권한 체크
                        chatRoomMemberRepository.existsByRoomIdAndUserIdAndIsActiveTrue(roomId, inviterId)
                                .flatMap(has -> {
                                    if (!has) return Mono.error(new IllegalStateException("초대 권한이 없습니다."));
                                    if (room.getType() == ChatRoom.ChatRoomType.PRIVATE) {
                                        // PRIVATE → 기존 로직으로 위임 (한 명 남음+1명 초대=NEW_PRIVATE / 그 외=NEW_GROUP)
                                        return createGroupRoomFromPrivate(roomId, inviterId, distinctTargets)
                                                .map(created -> {
                                                    InviteResult.Outcome oc =
                                                            (created.getType() == ChatRoom.ChatRoomType.GROUP)
                                                                    ? InviteResult.Outcome.NEW_GROUP
                                                                    : InviteResult.Outcome.NEW_PRIVATE;
                                                    return new InviteResult(oc, created.getId(), 0, 0, 0);
                                                });
                                    } else { // GROUP
                                        return inviteIntoExistingGroup(room, inviterId, distinctTargets);
                                    }
                                })
                );

    }

    /**
     * 기존 GROUP 방에 멤버 초대(신규 insert / 비활성→활성 전환 / 이미 활성은 스킵)
     */
    private Mono<InviteResult> inviteIntoExistingGroup(ChatRoom room, Long inviterId, List<Long> targets) {
        final Long roomId = room.getId();

        // 1) 초대 대상 유저 존재 검증
        return userRepository.findAllById(targets)
                .collectList()
                .flatMap(foundUsers -> {
                    if (foundUsers.size() != targets.size()) {
                        Set<Long> foundIds = foundUsers.stream().map(User::getId).collect(Collectors.toSet());
                        List<Long> missing = targets.stream().filter(id -> !foundIds.contains(id)).toList();
                        return Mono.error(new IllegalStateException("존재하지 않는 사용자: " + missing));
                    }

                    // 2) 현재 멤버 상태 로딩(활/비 모두)
                    return chatRoomMemberRepository.findByRoomId(roomId)
                            .collectList()
                            .flatMap(existingMembers -> {
                                Map<Long, ChatRoomMember> byUser = new HashMap<>();
                                for (ChatRoomMember m : existingMembers) {
                                    byUser.put(m.getUserId(), m);
                                }

                                // 현재 활성 멤버 수
                                int activeCount = (int) existingMembers.stream()
                                        .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                                        .count();

                                // 분류: 이미 active / inactive(재활성 대상) / 완전 신규
                                List<Long> alreadyActive = targets.stream()
                                        .filter(uid -> {
                                            ChatRoomMember m = byUser.get(uid);
                                            return m != null && Boolean.TRUE.equals(m.getIsActive());
                                        })
                                        .toList();

                                List<ChatRoomMember> toReactivate = targets.stream()
                                        .map(byUser::get)
                                        .filter(m -> m != null && Boolean.FALSE.equals(m.getIsActive()))
                                        .map(m -> {
                                            m.setIsActive(true);
                                            m.setJoinedAt(Instant.now());
                                            m.setLeftAt(null);
                                            return m;
                                        })
                                        .toList();

                                List<ChatRoomMember> toInsert = targets.stream()
                                        .filter(uid -> !byUser.containsKey(uid))
                                        .map(uid -> ChatRoomMember.builder()
                                                .roomId(roomId)
                                                .userId(uid)
                                                .isActive(true)
                                                .build())
                                        .toList();

                                int willBeActiveAdded = toReactivate.size() + toInsert.size();
                                int finalActiveCount = activeCount + willBeActiveAdded;
                                if (finalActiveCount > ChatRoom.MAX_MEMBERS) {
                                    return Mono.error(new IllegalStateException(
                                            "그룹 채팅방 최대 인원(" + ChatRoom.MAX_MEMBERS + ")을 초과합니다."));
                                }

                                // 3) 저장(재활성 → 신규 순)
                                Mono<Void> reactivateMono = (toReactivate.isEmpty())
                                        ? Mono.empty()
                                        : chatRoomMemberRepository.saveAll(toReactivate).then();

                                Mono<Void> insertMono = (toInsert.isEmpty())
                                        ? Mono.empty()
                                        : chatRoomMemberRepository.saveAll(toInsert).then();

                                return reactivateMono.then(insertMono)
                                        .thenReturn(new InviteResult(
                                                InviteResult.Outcome.UPDATED_GROUP,
                                                roomId,
                                                toInsert.size(),
                                                toReactivate.size(),
                                                alreadyActive.size()
                                        ));
                            });
                });
    }




    // 방 하나에 대한 RoomListItem 생성 로직
    private Function<Long, Mono<ChatRoomDto.RoomListItem>> buildListItemForRoom(Long me) {
        return roomId -> chatRoomRepository.findById(roomId)
                .switchIfEmpty(Mono.empty())
                .flatMap(room -> {
                    // 최근 메시지 1개
                    Mono<Optional<ChatMessage>> lastMsgOpt =
                            chatMessageRepository.findLastMessageByRoomId(roomId)
                                    .map(Optional::of)
                                    .defaultIfEmpty(Optional.empty());

                    // 표시 이름
                    Mono<String> displayNameMono = switch (room.getType()) {
                        case PRIVATE ->
                            // 활성/비활성 모두에서 me 아닌 상대 1명 찾기
                        chatRoomMemberRepository.findByRoomId(roomId)
                                .map(ChatRoomMember::getUserId)
                                .filter(uid -> !uid.equals(me))
                                .next()
                                .flatMap(userRepository::findById)
                                .map(User::getNickname)
                                .defaultIfEmpty("(알 수 없음)");
                        case GROUP -> Mono.justOrEmpty(room.getName()).defaultIfEmpty("(이름 없음)");
                    };

                    // 활성 멤버 수
                    Mono<Integer> memberCountMono =
                            chatRoomMemberRepository.findByRoomIdAndIsActiveTrue(roomId)
                                    .count().map(Long::intValue);

                    return Mono.zip(lastMsgOpt, displayNameMono, memberCountMono)
                            .map(tuple -> {
                                var lastOpt = tuple.getT1(); // Optional<ChatMessage>
                                var displayName = tuple.getT2(); // String
                                var memberCount = tuple.getT3(); // Integer

                                var last = lastOpt.orElse(null);
                                var lastType = (last == null ? null : last.getType().name());
                                var lastText = (last == null ? null : last.getContent());
                                var lastAt   = (last == null ? room.getCreatedAt() : last.getCreatedAt());

                                return ChatRoomDto.RoomListItem.builder()
                                        .roomId(room.getId())
                                        .type(room.getType().name())
                                        .displayName(displayName)
                                        .lastMessageType(lastType)
                                        .lastMessageText(lastText)
                                        .lastMessageAt(lastAt)
                                        .memberCount(memberCount)
                                        .build();
                            });
                });
    }

    private List<User> orderMembersWithFirst(Long firstUserId, List<User> users) {
        Comparator<User> nickThenId = Comparator.comparing(User::getNickname)
                .thenComparing(User::getId);
        User first = users.stream().filter(u -> u.getId().equals(firstUserId)).findFirst().orElse(null);
        List<User> others = users.stream().filter(u -> !u.getId().equals(firstUserId))
                .sorted(nickThenId).toList();
        ArrayList<User> ordered = new ArrayList<>(users.size());
        if (first != null) ordered.add(first);
        ordered.addAll(others);
        return ordered;
    }

    private String buildGroupName(List<User> orderedMembers){
        List<String> names = orderedMembers.stream().map(User::getNickname).toList();
        String name = (names.size() <= 5)
                ? String.join(",", names)
                : String.join(",", names.subList(0, 5)) + " 외 " + (names.size() -5) + "명";
        if (name.length() > 255) { // 길이 방어
            name = name.substring(0, 252) + "...";
        }
        return name;
    }

    // TODO: 채팅방 상세 조회 (멤버 목록 포함) 추가하기


}
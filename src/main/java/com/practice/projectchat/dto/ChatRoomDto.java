package com.practice.projectchat.dto;

import lombok.Builder;
import lombok.Getter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class ChatRoomDto {

    @Getter
    @Builder
    public static class RoomListItem {
        private Long roomId;
        private String type;
        private String displayName;
        private String lastMessageType;
        private String lastMessageText;
        private Instant lastMessageAt;
        private Integer memberCount;
    }

    // ---- 요청 DTO ----
    @Getter
    public static class CreatePrivateRequest {
        @NotNull
        private Long friendId;
    }

    @Getter
    public static class CreateGroupRequest {
        // creatorId는 토큰에서 읽음
        @NotEmpty
        private List<Long> memberIds;
    }

    @Getter
    public static class InviteRequest {
        @NotEmpty
        private List<Long> userIds;
    }

    // ---- 응답 DTO ----
    @Getter
    @Builder
    public static class RoomCreatedResponse {
        private Long roomId;
        private String type; // "PRIVATE"/"GROUP"
        private String name; // GROUP이면 name, PRIVATE는 null
    }

    @Getter
    @Builder
    public static class InviteResultResponse {
        private String outcome;           // UPDATED_GROUP / NEW_GROUP / NEW_PRIVATE
        private Long roomId;              // 결과 방 id
        private int addedCount;           // 신규 insert 수
        private int reactivatedCount;     // 비활성→활성
        private int skippedAlreadyActive; // 이미 활성이라 스킵
    }
}
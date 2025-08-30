package com.practice.projectchat.dto;

import com.practice.projectchat.domain.FriendShip;
import lombok.Builder;
import lombok.Getter;

public class FriendShipDto {

    @Getter
    @Builder
    public static class FriendInfo {
        private Long friendId;
        private String nickname;
        private String status;

        public static FriendInfo from(FriendShip fs, String nickname) {
            return FriendInfo.builder()
                    .friendId(fs.getFriendUserId())
                    .nickname(nickname)
                    .status(fs.getStatus().name())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class FriendDeleteResponse {
        private Long friendId;
        private String message;
    }

}
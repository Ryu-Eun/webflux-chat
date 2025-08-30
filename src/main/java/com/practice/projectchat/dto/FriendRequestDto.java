package com.practice.projectchat.dto;

import com.practice.projectchat.domain.FriendRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto {

    @Getter
    @Setter
    public static class SendRequest{
        private Long receiverId;
    }

    @Getter
    @Setter
    public static class RequestInfo{
        private Long id;
        private Long requesterId;
        private Long receiverId;
        private String status;

        // 자바객체 -> 응답 dto
        public static RequestInfo fromEntity(FriendRequest req) {
            RequestInfo dto = new RequestInfo();
            dto.setId(req.getId());
            dto.setRequesterId(req.getRequesterId());
            dto.setReceiverId(req.getReceiverId());
            dto.setStatus(req.getStatus().name());
            return dto;
        }
    }

}
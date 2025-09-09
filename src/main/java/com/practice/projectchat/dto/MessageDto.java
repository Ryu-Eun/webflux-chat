package com.practice.projectchat.dto;

import com.practice.projectchat.domain.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

public class MessageDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SendRequest{
        //TODO: 일단 TEXT만 허용시켜서 MVP 달성시키고, 이후에 IMAGE, SYSTEM도 추가
        @NotBlank
        @Pattern(regexp = "TEXT")
        private String type;

        @NotBlank
        @Size(min = 1, max = 2000, message = "메시지 글자수는 1~2000자 제한입니다.")
        private String content;

    }

    @Getter
    @Builder
    public static class MessageResponse{
        private Long id;
        private Long roomId;
        private Long senderId;
        private String type;
        private String content;
        private Instant createdAt;

        public static MessageResponse fromEntity(ChatMessage message){
            return MessageResponse.builder()
                    .id(message.getId())
                    .roomId(message.getRoomId())
                    .senderId(message.getSenderId())
                    .type(message.getType().name())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }

}
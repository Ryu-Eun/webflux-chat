package com.practice.projectchat.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @Column("id")
    private Long id;

    @Column("room_id")
    private Long roomId;

    @Column("sender_id")
    private Long senderId;

    @Column("content")
    private String content;

    @Column("type")
    private MessageType type;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @Version
    @Column("version")
    private Long version;

    public enum MessageType {
        TEXT,
        IMAGE,
        SYSTEM,  // 방 생성, 멤버 퇴장같은 이벤트
        INVITE   // 시스템 이벤트 (방 생성, 초대, 퇴장 등)
    }
}

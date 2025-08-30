package com.practice.projectchat.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table(name = "chat_room_members")
public class ChatRoomMember {

    @Id
    @Column("id")
    private Long id;

    @Column("room_id")
    private Long roomId;

    @Column("user_id")
    private Long userId;

    @Column("is_active")
    private Boolean isActive;

    @Column("joined_at")
    private Instant joinedAt;

    @Column("left_at")
    private Instant leftAt; // 초기값 null

    @Version
    @Column("version")
    private Long version;

}

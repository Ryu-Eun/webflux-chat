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

    // 방에 나갔다고 row를 삭제시키지않고, 히스토리를 활용할 수도 있기 때문에 isActive 컬럼으로 true/false 시킨다
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

package com.practice.projectchat.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table(name = "chat_rooms")
public class ChatRoom {

    public static final int MAX_MEMBERS = 50;

    @Id
    @Column("id")
    private Long id;

    // 1:1일 경우 방이름은 상대방 닉네임, Group일 경우 상대방 닉네임 나열
    // 1:1은 name 컬럼에 저장하지 않고, 상대방 닉네임으로 동적 표시. Group은 name 컬럼을 두고 저장, 이후에 수정 가능
    @Column("name")
    private String name;

    @Column("type")
    private ChatRoomType type;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    @Column("version")
    private Long version;

    public enum ChatRoomType {
        PRIVATE, // 1:1
        GROUP    // 그룹방
    }

}
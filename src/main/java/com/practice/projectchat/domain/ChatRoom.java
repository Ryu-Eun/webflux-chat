package com.practice.projectchat.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;


// TODO: A와 B가 동시에 PRIVATE 채팅방을 만들면 두개가 생길 수 있음.(동시성 문제) -> 나중에 해결
@Getter
@Builder
@Table(name = "chat_rooms")
public class ChatRoom {

    public static final int MAX_MEMBERS = 50;

    @Id
    @Column("id")
    private Long id;

    // 1:1일 경우 방이름은 상대방 닉네임, Group일 경우 상대방 닉네임 나열
    // 1:1은 name 컬럼에 저장하지 않고, 상대방 닉네임으로 동적 표시. Group은 name컬럼에 해당 구성원들의 이름들 나열로 들어감.
    // TODO: 각자 바라보는 대화창의 이름을 다르게 해주려면 테이블을 하나 더 생성해야하는데 그럼 초기개발에 너무 복잡해지니까 나중으로 미루기, 그리고 만약 대화방을 처음 만든 사용자가 대화방이름 변경 권한을 가지게 할수도 있는데, 그것도 복잡해질거같아서 넘김
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
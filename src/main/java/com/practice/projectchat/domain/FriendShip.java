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
@Table(name = "friendships")
public class FriendShip {

    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("friend_user_id")
    private Long friendUserId;

    @Column("status")
    private FriendshipStatus status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    @Column("version")
    private Long version;

    public enum FriendshipStatus {
        ACTIVE,   // 정상 친구
        BLOCKED,  // 차단된 친구
        DELETED   // 삭제됨 (soft delete)
    }

}

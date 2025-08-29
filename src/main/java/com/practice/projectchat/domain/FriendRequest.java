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
@Table(name = "friend_requests")
public class FriendRequest {

    @Id
    @Column("id")
    private Long id;

    @Column("requester_id")
    private Long requesterId;

    @Column("receiver_id")
    private Long receiverId;

    @Column("status")
    private RequestStatus status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    @Column
    private Long version;

    public enum RequestStatus {
        PENDING,   // 대기
        ACCEPTED,  // 수락됨
        REJECTED,  // 거절됨
    }

}
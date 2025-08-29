package com.practice.projectchat.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table(name = "black_list")
public class BlockList {

    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId; // 차단 건 사람

    @Column("blocked_user_id")
    private Long blockedUserId; // 차단 당한 사람

    @CreatedDate
    @Column("createdAt")
    private Instant createdAt;

}

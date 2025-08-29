package com.practice.projectchat.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @Column("id")
    private Long id; // unique

    @Column("login_id")
    private String loginId;

    @Column("password_hash")
    private String passwordHash;

    @Column("nickname")
    private String nickname;

    @Column("friend_code")
    private String friendCode; // 대문자알파벳,숫자 섞은 8자리. unique

    @Column("status")
    private UserStatus status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt; // DB에서는 timestamptz(UTC) 사용

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column("created_by")
    private String createdBy;

    @LastModifiedBy
    @Column("updated_by")
    private String updatedBy;

    @Version
    @Column("version")
    private Long version; // 프로필/상태/보안 위한 낙관적락


    public enum UserStatus{
        ACTIVE,       // 정상적으로 가입되어 활동 가능한 상태
        BLOCKED,      // 규칙위반,악용계정으로 운영자에 의해 차단된 상태
        DEACTIVATED,  // 사용자가 스스로 비활성화한 계정 (탈퇴예약)
        DELETED       // 계정이 완전히 삭제됨 (메시지/대화방 이력은 "삭제된 사용자"로만 남김)
    }

}
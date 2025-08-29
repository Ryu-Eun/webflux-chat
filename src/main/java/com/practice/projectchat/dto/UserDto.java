package com.practice.projectchat.dto;

import com.practice.projectchat.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

public class UserDto {

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SignupRequest {

        // 영문 소문자 + 숫자만 허용, 총 길이 4~32자
        @NotBlank(message = "LoginId is required")
        @Size(min = 4, max = 32)
        @Pattern(regexp = "^[a-z0-9]{4,32}$",
                message = "loginId는 영문 소문자와 숫자만 사용할 수 있습니다(4~32자).")
        private String loginId;

        // 영문 1+, 숫자 1+, 특수문자(@#$!?) 1+ 포함, 총 길이 8~64
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!?])[A-Za-z\\d@#$!?]{8,64}$",
                message = "비밀번호는 영문, 숫자, 특수문자(@#$!?)를 각 1자 이상 포함해야 합니다."
        )
        private String password;

        // 닉네임 2~10자 제한
        @NotBlank
        @Size(min = 2, max = 10)
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest{
        @NotBlank
        @Size(min = 4, max = 32)
        @Pattern(regexp = "^[a-z0-9]{4,32}$", message = "loginId는 소문자 영문/숫자만 4~32자입니다.")
        private String loginId;

        @NotBlank
        @Size(min = 8, max = 64)
        private String password;
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupResponse {
        private Long userId;
        private String nickname;
        private String friendCode;

        // User -> Response
        public static SignupResponse of(Long userId, String nickname, String friendCode) {
            return SignupResponse.builder()
                    .userId(userId)
                    .nickname(nickname)
                    .friendCode(friendCode)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginResponse {
        private String tokenType;   // Bearer
        private String accessToken; // JWT토큰
        private long expiresAt;     // 만료 시각
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MeResponse {
        private Long userId;
        private String loginId;
        private String nickname;
        private String friendCode;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;

        public static UserDto.MeResponse toMeResponse(User u) {
            return UserDto.MeResponse.builder()
                    .userId(u.getId())
                    .loginId(u.getLoginId())
                    .nickname(u.getNickname())
                    .friendCode(u.getFriendCode())
                    .status(u.getStatus().name())
                    .createdAt(u.getCreatedAt())
                    .updatedAt(u.getUpdatedAt())
                    .build();
        }
    }


}
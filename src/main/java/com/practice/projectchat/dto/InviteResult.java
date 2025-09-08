package com.practice.projectchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InviteResult {

    private final Outcome outcome;
    private final Long roomId;        // UPDATED_GROUP이면 기존 roomId, NEW_*면 새로 만들어진 roomId
    private final int addedCount;     // 새 insert 수
    private final int reactivatedCount; // 비활성 → 활성 전환 수
    private final int skippedAlreadyActive; // 이미 active라 스킵된 수

    public enum Outcome {
        UPDATED_GROUP,  // 기존 GROUP 방에 멤버 추가/재활성화
        NEW_GROUP,      // PRIVATE에서 초대로 새 GROUP 방 생성
        NEW_PRIVATE     // PRIVATE에서 초대로 새 PRIVATE 방 생성(한 명만 남아있고 1명 초대)
    }

}
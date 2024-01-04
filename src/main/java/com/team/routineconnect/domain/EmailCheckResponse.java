package com.team.routineconnect.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailCheckResponse {
    SUCCESS(true, "사용 가능한 아이디입니다."),
    ERROR(false, "이미 사용 중인 아이디입니다.");

    private final boolean status;
    private final String message;
}

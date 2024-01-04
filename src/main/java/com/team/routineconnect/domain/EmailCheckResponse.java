package com.team.routineconnect.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum EmailCheckResponse {
    SUCCESS(false, "사용 가능한 아이디입니다."),
    ERROR(true, "이미 사용 중인 아이디입니다.");

    private final boolean isDuplicated;
    private final String message;
}

package kr.online.routineconnect.config.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Error {

    EXPIRED_TOKEN("만료된 토큰입니다."),
    BAD_TOKEN("잘못된 토큰입니다."),
    AUTHORIZATION_FAILED("인증에 실패하였습니다.");

    private final String message;
}

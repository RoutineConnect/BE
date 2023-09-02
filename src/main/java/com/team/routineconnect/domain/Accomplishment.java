package com.team.routineconnect.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Accomplishment {
    CLEAR("clear"),
    INCOMPLETE("incomplete"),
    FAIL("fail"),
    IN_PROGRESS("in_progress");

    private final String value;
}



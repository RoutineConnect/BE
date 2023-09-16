package com.team.routineconnect.dto;

import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.Routine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RoutineWithAccomplishment {
    private Routine routine;
    private Accomplishment accomplishment;
}

package com.team.routineconnect.dto;

import com.team.routineconnect.domain.Accomplishment;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
public class RoutineWithAccomplishment {
    private Long routine_id;
    private String title;
    private String hour;
    private Byte repeating_days;
    private Boolean shared;
    private LocalDateTime created_date;
    private LocalDateTime ended_date;
    private Float position;
    private Accomplishment accomplishment;
}

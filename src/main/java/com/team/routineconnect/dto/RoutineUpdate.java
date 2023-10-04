package com.team.routineconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RoutineUpdate {
    @NotNull
    private Long routine_id;
    @NotNull
    private Float position;
}

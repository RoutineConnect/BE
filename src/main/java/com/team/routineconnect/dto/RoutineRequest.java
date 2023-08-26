package com.team.routineconnect.dto;

import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static java.time.LocalDateTime.MIN;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RoutineRequest {

    private String title;
    private String hour;
    private Byte routineDay;
    private Boolean shared;
    private LocalDateTime createdDate;
    private LocalDateTime endedDate;

    public Routine toEntity(User user, EnumSet<DayOfWeek> repeatingDays) {
        return Routine.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .repeatingDays(repeatingDays)
                .shared(shared)
                .createdDate(createdDate.with(MIN))
                .endedDate(endedDate != null ? endedDate.with(MIN) : null)
                .build();
    }
}

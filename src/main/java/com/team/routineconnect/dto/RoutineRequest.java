package com.team.routineconnect.dto;

import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public Routine toEntity(User user) {
        return Routine.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .routineDay(routineDay)
                .shared(shared)
                .createdDate(createdDate)
                .endedDate(endedDate)
                .build();
    }
}

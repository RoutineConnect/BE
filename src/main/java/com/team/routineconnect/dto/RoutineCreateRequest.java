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
public class RoutineCreateRequest {
    private String title;
    private Byte routineDay;
    private String hour;
    private Boolean shared;

    public Routine toEntity(User user, LocalDateTime createdDate) {
        return Routine.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .shared(shared)
                .createdDate(createdDate)
                .build();
    }
}

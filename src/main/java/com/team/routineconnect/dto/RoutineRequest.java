package com.team.routineconnect.dto;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
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
    private EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public Routine toEntity(User user) {
        return Routine.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .repeatingDays(routineDayToEntityAttribute())
                .shared(shared)
                .createdDate(createdDate.with(MIN))
                .endedDate(endedDate != null ? endedDate.with(MIN) : null)
                .build();
    }

    public EnumSet<DayOfWeek> routineDayToEntityAttribute() {
        return enumSetToBitmaskConverter.convertToEntityAttribute(this.routineDay);
    }
}

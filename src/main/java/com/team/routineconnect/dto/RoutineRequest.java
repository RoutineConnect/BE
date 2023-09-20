package com.team.routineconnect.dto;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static java.time.LocalTime.MIN;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoutineRequest {

    private String title;
    private String hour;
    private Byte routine_day;
    private Boolean shared;
    private LocalDateTime created_date;
    private LocalDateTime ended_date;
    @Getter(AccessLevel.NONE)
    private EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public Routine toEntity(User user) {
        return Routine.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .repeatingDays(routineDayToEntityAttribute())
                .shared(shared)
                .createdDate(created_date)
                .endedDate(ended_date)
                .build();
    }

    public EnumSet<DayOfWeek> routineDayToEntityAttribute() {
        return enumSetToBitmaskConverter.convertToEntityAttribute(this.routine_day);
    }
}

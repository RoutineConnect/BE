package com.team.routineconnect.dto;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.repository.HourRepository;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoutineRequest {

    @NotEmpty(message = "제목은 한 글자 이상이어야합니다.")
    private String title;
    private Hour hour;
    @NotNull
    private Byte routine_day;
    @NotNull
    private Boolean shared;
    @NotNull
    private LocalDateTime created_date;
    private LocalDateTime ended_date;
    @Getter(AccessLevel.NONE)
    private EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    @Getter(AccessLevel.NONE)
    private HourRepository hourRepository;

    public Routine toEntity(User user) {
        if (hour != null) {
            if (hour.getId() == null) {
                hour.setUser(user);
                hour = hourRepository.save(hour);
            } else {
                hour = hourRepository.findById(hour.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Illegal hour ID"));
            }
        }

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

package com.team.routineconnect.dto;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.repository.HourRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoutineRequest {

    @NotEmpty(message = "제목은 한 글자 이상이어야합니다.")
    private String title;
    private String hour;
    @NotNull
    private String routine_day;
    @NotNull
    private Boolean shared;
    @NotNull
    private LocalDate created_date;
    private LocalDate ended_date;

    @Getter(AccessLevel.NONE)
    private HourRepository hourRepository;

    public Routine toEntity(User user, EnumSetToBitmaskConverter enumSetToBitmaskConverter) {

        return Routine.builder()
                .user(user)
                .title(title)
                .hour(setHourWith(user))
                .repeatingDays(routineDayToEntityAttribute(enumSetToBitmaskConverter))
                .createdDate(created_date)
                .endedDate(ended_date)
                .build();
    }

    public Hour setHourWith(User user) {
        return Optional.ofNullable(hour)
                .flatMap(existingHour -> hourRepository.findByHourAndUser(existingHour, user))
                .orElseGet(() -> hour != null ? hourRepository.save(
                        Hour.builder()
                                .hour(hour)
                                .user(user)
                                .build()
                ) : null);
    }


    public EnumSet<DayOfWeek> routineDayToEntityAttribute(EnumSetToBitmaskConverter enumSetToBitmaskConverter) {
        return enumSetToBitmaskConverter.convertToEntityAttribute((byte) Integer.parseInt(this.routine_day, 2));
    }
}

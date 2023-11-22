package com.team.routineconnect.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.Item;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.repository.HourRepository;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoutineRequest {

    @NotEmpty(message = "제목은 한 글자 이상이어야합니다.")
    private String title;
    private HourDto hour;
    @NotNull
    private Byte routine_day;
    @NotNull
    private Boolean shared;
    @NotNull
    private LocalDate created_date;
    private LocalDate ended_date;

    @Getter(AccessLevel.NONE)
    private HourRepository hourRepository;

    public Item toEntity(
            User user
            , EnumSetToBitmaskConverter enumSetToBitmaskConverter,
            ObjectMapper objectMapper,
            HourRepository hourRepository
    ) {
        Hour hour = this.hour.toEntity(objectMapper);
        if (hour.getId() == null) {
            hour.setUser(user);
            hour = hourRepository.save(hour);
        } else {
            Optional<Hour> hourOptional = hourRepository.findById(hour.getId());
            if (hourOptional.isEmpty() || !user.equals(hourOptional.get().getUser())) {
                throw new IllegalArgumentException("Invalid hour ID");
            }
        }

        return Item.builder()
                .user(user)
                .title(title)
                .hour(hour)
                .repeatingDays(routineDayToEntityAttribute(enumSetToBitmaskConverter))
                .shared(shared)
                .createdDate(created_date)
                .endedDate(ended_date)
                .build();
    }

    public EnumSet<DayOfWeek> routineDayToEntityAttribute(EnumSetToBitmaskConverter enumSetToBitmaskConverter) {
        return enumSetToBitmaskConverter.convertToEntityAttribute(this.routine_day);
    }
}

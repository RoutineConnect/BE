package com.team.routineconnect.mapper;

import com.querydsl.core.Tuple;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.dto.RoutineWithAccomplishment;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import static com.team.routineconnect.domain.QDayOrder.dayOrder;

@Component
@Configurable
@AllArgsConstructor
public class ResultMapper {

    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public RoutineWithAccomplishment mapToDto(Tuple result) {
        EnumSet<DayOfWeek> enumSet = EnumSet.noneOf(DayOfWeek.class);
        Set<DayOfWeek> repeatingDays = result.get(dayOrder.routine.repeatingDays);
        enumSet.addAll(repeatingDays);

        return RoutineWithAccomplishment.builder()
                .routine_id(result.get(dayOrder.routine.id))
                .title(result.get(dayOrder.routine.title))
                .hour(result.get(dayOrder.routine.hour))
                .repeating_days(enumSetToBitmaskConverter.convertToDatabaseColumn(enumSet))
                .shared(result.get(dayOrder.routine.shared))
                .created_date(result.get(dayOrder.routine.createdDate))
                .ended_date(result.get(dayOrder.routine.endedDate))
                .position(result.get(dayOrder.position))
                .accomplishment(result.get(dayOrder.accomplishment))
                .build();
    }
}

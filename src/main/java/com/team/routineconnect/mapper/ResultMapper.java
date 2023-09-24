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

import static com.team.routineconnect.domain.QItemOrder.itemOrder;

@Component
@Configurable
@AllArgsConstructor
public class ResultMapper {

    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public RoutineWithAccomplishment mapToDto(Tuple result) {
        EnumSet<DayOfWeek> enumSet = EnumSet.noneOf(DayOfWeek.class);
        Set<DayOfWeek> repeatingDays = result.get(itemOrder.routine.repeatingDays);
        enumSet.addAll(repeatingDays);

        return RoutineWithAccomplishment.builder()
                .routine_id(result.get(itemOrder.routine.id))
                .title(result.get(itemOrder.routine.title))
                .hour(result.get(itemOrder.routine.hour))
                .repeating_days(enumSetToBitmaskConverter.convertToDatabaseColumn(enumSet))
                .shared(result.get(itemOrder.routine.shared))
                .created_date(result.get(itemOrder.routine.createdDate))
                .ended_date(result.get(itemOrder.routine.endedDate))
                .position(result.get(itemOrder.position))
                .accomplishment(result.get(itemOrder.accomplishment))
                .build();
    }
}

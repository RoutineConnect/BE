package com.team.routineconnect.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineWithAccomplishment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.team.routineconnect.domain.QDayOrder.dayOrder;

@AllArgsConstructor
@Repository
public class DayOrderRepositoryImpl implements DayOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<LocalDate> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date) {
        LocalDate maxDate = queryFactory
                .select(dayOrder.date.max())
                .from(dayOrder)
                .where(dayOrder.day.eq(day)
                        .and(dayOrder.date.loe(date))
                        .and(dayOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Optional<LocalDate> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date) {
        LocalDate maxDate = queryFactory
                .select(dayOrder.date.max())
                .from(dayOrder)
                .where(dayOrder.day.eq(day)
                        .and(dayOrder.date.lt(date))
                        .and(dayOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Optional<Float> findMaxPositionByUserAndDate(User user, LocalDate date) {
        Float maxPosition = queryFactory
                .select(dayOrder.position.max())
                .from(dayOrder)
                .where(dayOrder.date.eq(date)
                        .and(dayOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxPosition);
    }

    @Override
    public List<LocalDate> findDatesByUserAndDayAndDayGreaterThan(User user, DayOfWeek day, LocalDate date) {
        return queryFactory
                .select(dayOrder.date)
                .from(dayOrder)
                .where(dayOrder.day.eq(day)
                        .and(dayOrder.date.gt(date))
                        .and(dayOrder.user.eq(user)))
                .fetch();
    }

    @Override
    public List<RoutineWithAccomplishment> findRoutinesByUserAndDate(User user, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        Optional<LocalDate> maxDateOptional = findMaxDateByUserAndDayAndDateBefore(user, day, date);

        if (maxDateOptional.isPresent()) {
            List<Tuple> results = queryFactory
                    .select(dayOrder.routine, dayOrder.accomplishment)
                    .from(dayOrder)
                    .where(dayOrder.day.eq(day)
                            .and(dayOrder.date.loe(date))
                            .and(dayOrder.user.eq(user)))
                    .fetch();

            return results.stream()
                    .map(tuple -> new RoutineWithAccomplishment(
                            tuple.get(dayOrder.routine),
                            tuple.get(dayOrder.accomplishment)))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Float findAchievementByUserAndDate(User user, LocalDate date) {
        float totalRoutines = (float) findRoutinesByUserAndDate(user, date).size();
        float clearRoutines = (float) queryFactory
                .select(Wildcard.count)
                .from(dayOrder)
                .where(dayOrder.accomplishment.eq(Accomplishment.CLEAR))
                .fetch().get(0);

        return totalRoutines != 0 ? clearRoutines / totalRoutines * 100 : 0;
    }
}

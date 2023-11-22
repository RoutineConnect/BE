package com.team.routineconnect.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.team.routineconnect.domain.QItemOrder.itemOrder;

@AllArgsConstructor
@Repository
public class ItemOrderRepositoryImpl implements ItemOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<LocalDate> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date) {
        LocalDate maxDate = queryFactory
                .select(itemOrder.date.max())
                .from(itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.loe(date))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Optional<LocalDate> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date) {
        LocalDate maxDate = queryFactory
                .select(itemOrder.date.max())
                .from(itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.lt(date))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Optional<Float> findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date) {
        Float maxPosition = queryFactory
                .select(itemOrder.position.max())
                .from(itemOrder)
                .where(itemOrder.date.eq(date)
                        .and(itemOrder.day.eq(day))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxPosition);
    }

    @Override
    public List<LocalDate> findDatesByUserAndDayAndDateAfter(User user, DayOfWeek day, LocalDate date) {
        return queryFactory
                .select(itemOrder.date)
                .from(itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.gt(date))
                        .and(itemOrder.user.eq(user)))
                .fetch();
    }

    @Override
    public List<ItemOrder> findRoutinesByUserRoutineIsNotNullAndDateLessThanEqual(User user, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        Optional<LocalDate> maxDateOptional = findMaxDateByUserAndDayAndDateBefore(user, day, date);

        if (maxDateOptional.isPresent()) {
            return queryFactory
                    .selectFrom(itemOrder)
                    .where(itemOrder.day.eq(day)
                            .and(itemOrder.date.loe(date))
                            .and(itemOrder.user.eq(user))
                            .and(itemOrder.item.isNotNull()))
                    .fetch();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Float findAchievementByUserAndDate(User user, LocalDate date) {
        float totalRoutines = (float) findRoutinesByUserRoutineIsNotNullAndDateLessThanEqual(user, date).size();
        float clearRoutines = (float) queryFactory
                .select(Wildcard.count)
                .from(itemOrder)
                .where(itemOrder.accomplishment.eq(Accomplishment.CLEAR))
                .fetch().get(0);

        return totalRoutines != 0 ? clearRoutines / totalRoutines * 100 : 0;
    }
}

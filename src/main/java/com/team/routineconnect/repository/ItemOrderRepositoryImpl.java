package com.team.routineconnect.repository;

import static com.team.routineconnect.domain.QItem.item;
import static com.team.routineconnect.domain.QItemOrder.itemOrder;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.ItemResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class ItemOrderRepositoryImpl implements ItemOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public LocalDate findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date) {
        return queryFactory
                .select(itemOrder.date.max())
                .from(itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.loe(date))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();
    }

    @Override
    public LocalDate findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date) {
        return queryFactory
                .select(itemOrder.date.max())
                .from(itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.lt(date))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();
    }

    @Override
    public double findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date) {
        Double position = queryFactory
                .select(itemOrder.position.max())
                .from(itemOrder)
                .where(itemOrder.date.eq(date)
                        .and(itemOrder.day.eq(day))
                        .and(itemOrder.user.eq(user)))
                .fetchOne();

        return position != null ? position : 0.0d;
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
    public List<ItemResponse> findRoutinesByUserRoutineIsNotNullAndDate(User user, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate maxDate = findMaxDateByUserAndDayAndDateLessThan(user, day, date);

        return maxDate != null ?
                queryFactory
                        .select(itemOrder.item.hour.hour, itemOrder.item.title, itemOrder.position,
                                itemOrder.accomplishment, itemOrder.retrospective)
                        .from(itemOrder)
                        .where(itemOrder.day.eq(day)
                                .and(itemOrder.date.eq(maxDate))
                                .and(itemOrder.user.eq(user))
                                .and(itemOrder.item.isNotNull()))
                        .innerJoin(item).on(itemOrder.item.eq(item))
                        .fetchJoin().fetch()
                        .stream()
                        .map(tuple -> ItemResponse.builder()
                                .hour(tuple.get(item.hour.hour))
                                .title(tuple.get(item.title))
                                .position(tuple.get(itemOrder.position))
                                .accomplishment(tuple.get(itemOrder.accomplishment))
                                .retrospective(tuple.get(itemOrder.retrospective))
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    public Float findAchievementByUserAndDate(User user, LocalDate date) {
        float totalRoutines = (float) findRoutinesByUserRoutineIsNotNullAndDate(user, date).size();
        float clearRoutines = (float) queryFactory
                .select(Wildcard.count)
                .from(itemOrder)
                .where(itemOrder.accomplishment.eq(true))
                .fetch().get(0);

        return totalRoutines != 0 ? clearRoutines / totalRoutines * 100 : 0;
    }
}

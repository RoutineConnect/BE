package com.team.routineconnect.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.QDayOrder;
import com.team.routineconnect.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class DayOrderRepositoryImpl implements DayOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<LocalDateTime> findMaxDateByUserAndDateAndDay(User user, LocalDateTime date, DayOfWeek day) {
        QDayOrder d = QDayOrder.dayOrder;
        LocalDateTime maxDate = queryFactory
                .select(d.date.max())
                .from(d)
                .where(d.day.eq(day)
                        .and(d.date.loe(date.with(LocalTime.MAX)))
                        .and(d.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Float findPositionByUserAndDateAndDay(User user, LocalDateTime date, DayOfWeek day){
        QDayOrder d = QDayOrder.dayOrder;
        return queryFactory
                .select(d.position)
                .from(d)
                .where(d.day.eq(day)
                        .and(d.date.eq(date))
                        .and(d.user.eq(user)))
                .fetchOne();
    }
}

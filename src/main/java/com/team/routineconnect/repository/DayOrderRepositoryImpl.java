package com.team.routineconnect.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.QDayOrder;
import com.team.routineconnect.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class DayOrderRepositoryImpl implements DayOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<LocalDateTime> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDateTime date) {
        QDayOrder d = QDayOrder.dayOrder;
        LocalDateTime maxDate = queryFactory
                .select(d.date.max())
                .from(d)
                .where(d.day.eq(day)
                        .and(d.date.loe(date))
                        .and(d.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Optional<LocalDateTime> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDateTime date) {
        QDayOrder d = QDayOrder.dayOrder;
        LocalDateTime maxDate = queryFactory
                .select(d.date.max())
                .from(d)
                .where(d.day.eq(day)
                        .and(d.date.lt(date))
                        .and(d.user.eq(user)))
                .fetchOne();

        return Optional.ofNullable(maxDate);
    }

    @Override
    public Float findMaxPositionByUserAndDate(User user, LocalDateTime date) {
        QDayOrder d = QDayOrder.dayOrder;

        return queryFactory
                .select(d.position.max())
                .from(d)
                .where(d.date.eq(date)
                        .and(d.user.eq(user)))
                .fetchOne();
    }

    @Override
    public List<LocalDateTime> findDatesByUserAndDayAndDayAfter(User user, DayOfWeek day, LocalDateTime date) {
        QDayOrder d = QDayOrder.dayOrder;

        return queryFactory
                .select(d.date)
                .from(d)
                .where(d.day.eq(day)
                        .and(d.date.goe(date))
                        .and(d.user.eq(user)))
                .fetch();
    }
}

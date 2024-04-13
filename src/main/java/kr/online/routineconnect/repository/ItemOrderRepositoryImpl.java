package kr.online.routineconnect.repository;

import static kr.online.routineconnect.domain.QAccomplishment.accomplishment1;
import static kr.online.routineconnect.domain.QHour.hour1;
import static kr.online.routineconnect.domain.QItem.item;
import static kr.online.routineconnect.domain.QItemOrder.itemOrder;
import static kr.online.routineconnect.domain.QItemOrderIgnore.itemOrderIgnore;
import static kr.online.routineconnect.domain.QRetrospect.retrospect;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import kr.online.routineconnect.domain.User;
import kr.online.routineconnect.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ItemOrderRepositoryImpl implements ItemOrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ItemResponse> findItemsByUserAndDate(User user, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();

        return queryFactory
                .select(Projections.fields(ItemResponse.class,
                        itemOrder.item.id,
                        itemOrder.id,
                        itemOrder.item.hour.hour,
                        itemOrder.item.title,
                        itemOrder.position,
                        accomplishment1.accomplishment,
                        retrospect.content))
                .from(itemOrder)
                .innerJoin(itemOrder.item, item)
                .leftJoin(itemOrder.item.hour, hour1)
                .leftJoin(itemOrder.accomplishment, accomplishment1)
                .leftJoin(itemOrder.retrospect, retrospect)
                .leftJoin(itemOrder, itemOrderIgnore.itemOrder)
                .where(itemOrder.day.eq(day)
                        .and(itemOrder.date.loe(date))
                        .and(itemOrder.user.eq(user))
                        .and(itemOrderIgnore.id.isNull()))
                .orderBy(itemOrder.position.asc())
                .fetchJoin().fetch();
    }

    @Override
    public double findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date) {
        Double position = queryFactory
                .select(itemOrder.position.max())
                .from(itemOrder)
                .leftJoin(itemOrderIgnore).on(itemOrder.id.eq(itemOrderIgnore.id)
                        .and(itemOrderIgnore.date.eq(date))
                        .and(itemOrderIgnore.day.eq(day))
                        .and(itemOrderIgnore.user.eq(user)))
                .where(itemOrder.date.eq(date)
                        .and(itemOrder.day.eq(day))
                        .and(itemOrder.user.eq(user))
                        .and(itemOrderIgnore.id.isNull()))
                .fetchOne();

        return position != null ? position : 0.0d;
    }
}

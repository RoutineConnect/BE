package com.team.routineconnect.repository;

import com.team.routineconnect.domain.Item;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrderRepository extends JpaRepository<ItemOrder, Long>, ItemOrderRepositoryCustom {
    List<ItemOrder> findByUserAndDateAndItemIsNotNull(User user, LocalDate date);

    List<ItemOrder> findByItemAndDayAndDateLessThanEqual(Item item, DayOfWeek day, LocalDate date);

    List<ItemOrder> findByUserAndDateAndItemNot(User user, LocalDate date, Item item);

    List<ItemOrder> findByUserAndDateOrderByPosition(User user, LocalDate date);

    List<ItemOrder> findByItemAndDayAndDateAfterOrderByDate(Item item, DayOfWeek day, LocalDate date);

    void deleteByItemAndDate(Item item, LocalDate date);

    void deleteByItemAndDayAndDateGreaterThan(Item item, DayOfWeek day, LocalDate date);

    void deleteAllByItemAndDayAndDateGreaterThan(Item item, DayOfWeek day, LocalDate date);

    List<ItemOrder> findByDateGreaterThanEqual(LocalDate date);

    Optional<ItemOrder> findTopByItemAndDayAndDateLessThanOrderByDateDesc(Item item, DayOfWeek day, LocalDate date);
}

package com.team.routineconnect.repository;

import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemOrderRepository extends JpaRepository<ItemOrder, Long>, ItemOrderRepositoryCustom {
    List<ItemOrder> findByUserAndDate(User user, LocalDate date);

    List<ItemOrder> findByUserAndRoutineAndDayAndDateLessThanEqual(User user, Routine routine, DayOfWeek day, LocalDate date);

    List<ItemOrder> findByUserAndDateAndRoutineNot(User user, LocalDate date, Routine routine);

    List<ItemOrder> findByUserAndDateOrderByPosition(User user, LocalDate date);

    List<ItemOrder> findByRoutineAndDateAfterOrderByDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);


    void deleteAllByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);

    List<ItemOrder> findByDateGreaterThanEqual(LocalDate date);

}

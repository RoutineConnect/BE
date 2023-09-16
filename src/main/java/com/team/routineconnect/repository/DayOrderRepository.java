package com.team.routineconnect.repository;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayOrderRepository extends JpaRepository<DayOrder, Long>, DayOrderRepositoryCustom {
    List<DayOrder> findByUserAndDate(User user, LocalDate date);

    List<DayOrder> findByUserAndRoutineAndDayAndDateLessThanEqual(User user, Routine routine, DayOfWeek day, LocalDate date);

    List<DayOrder> findByUserAndDateAndRoutineNot(User user, LocalDate date, Routine routine);

    List<DayOrder> findByUserAndDateOrderByPosition(User user, LocalDate date);

    List<DayOrder> findByRoutineAndDateAfterOrderByDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);


    void deleteAllByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);

    List<DayOrder> findByDateGreaterThanEqual(LocalDate date);

}

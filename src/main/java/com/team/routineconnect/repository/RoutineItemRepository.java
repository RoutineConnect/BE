package com.team.routineconnect.repository;

import com.team.routineconnect.domain.RoutineItem;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoutineItemRepository extends JpaRepository<RoutineItem, Long>, RoutineItemRepositoryCustom {
    List<RoutineItem> findByUserAndDate(User user, LocalDate date);

    List<RoutineItem> findByUserAndRoutineAndDayAndDateLessThanEqual(User user, Routine routine, DayOfWeek day, LocalDate date);

    List<RoutineItem> findByUserAndDateAndRoutineNot(User user, LocalDate date, Routine routine);

    List<RoutineItem> findByUserAndDateOrderByPosition(User user, LocalDate date);

    List<RoutineItem> findByRoutineAndDateAfterOrderByDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDate(Routine routine, LocalDate date);

    void deleteByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);


    void deleteAllByRoutineAndDayAndDateGreaterThan(Routine routine, DayOfWeek day, LocalDate date);

    List<RoutineItem> findByDateGreaterThanEqual(LocalDate date);

}

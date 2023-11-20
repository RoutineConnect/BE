package com.team.routineconnect.repository;

import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ItemOrderRepositoryCustom {
    Optional<LocalDate> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date);

    Optional<LocalDate> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date);

    Optional<Float> findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date);

    List<LocalDate> findDatesByUserAndDayAndDateAfter(User user, DayOfWeek day, LocalDate date);

    List<ItemOrder> findRoutinesByUserRoutineIsNotNullAndDateLessThanEqual(User user, LocalDate date);

    Float findAchievementByUserAndDate(User user, LocalDate date);
}

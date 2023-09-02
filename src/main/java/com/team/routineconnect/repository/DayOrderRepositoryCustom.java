package com.team.routineconnect.repository;

import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineWithAccomplishment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayOrderRepositoryCustom {
    Optional<LocalDate> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date);
    Optional<LocalDate> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date);
    Optional<Float> findMaxPositionByUserAndDate(User user, LocalDate date);
    List<LocalDate> findDatesByUserAndDayAndDayGreaterThan(User user, DayOfWeek day, LocalDate date);
    List<RoutineWithAccomplishment> findRoutinesByUserAndDate(User user, LocalDate date);
    Double findAchievementByUserAndDate(User user, LocalDate date);
}

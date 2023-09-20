package com.team.routineconnect.repository;

import com.querydsl.core.Tuple;
import com.team.routineconnect.domain.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayOrderRepositoryCustom {
    Optional<LocalDate> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date);
    Optional<LocalDate> findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date);
    Optional<Float> findMaxPositionByUserAndDate(User user, LocalDate date);
    List<LocalDate> findDatesByUserAndDayAndDateGreaterThan(User user, DayOfWeek day, LocalDate date);
    List<Tuple> findRoutinesByUserAndDate(User user, LocalDate date);
    Float findAchievementByUserAndDate(User user, LocalDate date);
}

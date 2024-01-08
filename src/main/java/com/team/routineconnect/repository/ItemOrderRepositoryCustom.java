package com.team.routineconnect.repository;

import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.ItemResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface ItemOrderRepositoryCustom {
    LocalDate findMaxDateByUserAndDayAndDateLessThan(User user, DayOfWeek day, LocalDate date);

    LocalDate findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDate date);

    double findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date);

    List<LocalDate> findDatesByUserAndDayAndDateAfter(User user, DayOfWeek day, LocalDate date);

    List<ItemResponse> findRoutinesByUserRoutineIsNotNullAndDate(User user, LocalDate date);

    Float findAchievementByUserAndDate(User user, LocalDate date);
}

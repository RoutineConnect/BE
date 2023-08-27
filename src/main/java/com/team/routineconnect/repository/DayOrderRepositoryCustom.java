package com.team.routineconnect.repository;

import com.team.routineconnect.domain.User;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DayOrderRepositoryCustom {
    Optional<LocalDateTime> findMaxDateByUserAndDayAndDateBefore(User user, DayOfWeek day, LocalDateTime date);
    Float findMaxPositionByUserAndDate(User user, LocalDateTime date);
    List<LocalDateTime> findDatesByUserAndDayAndDayAfter(User user, DayOfWeek day, LocalDateTime date);
}

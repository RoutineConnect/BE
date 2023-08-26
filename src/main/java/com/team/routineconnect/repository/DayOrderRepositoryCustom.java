package com.team.routineconnect.repository;

import com.team.routineconnect.domain.User;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DayOrderRepositoryCustom {
    Optional<LocalDateTime> findMaxDateByUserAndDateAndDay(User user, LocalDateTime date, DayOfWeek day);
    Float findMaxPositionByUserAndDate(User user, LocalDateTime date);
}

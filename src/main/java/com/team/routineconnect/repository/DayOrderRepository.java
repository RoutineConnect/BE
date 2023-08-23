package com.team.routineconnect.repository;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DayOrderRepository extends JpaRepository {
    List<DayOrder> findByUserAndDate(User user, LocalDateTime date);
}

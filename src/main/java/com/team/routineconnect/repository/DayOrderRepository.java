package com.team.routineconnect.repository;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayOrderRepository extends JpaRepository<DayOrder, Long>, DayOrderRepositoryCustom {
    List<DayOrder> findByUserAndDate(User user, LocalDateTime date);
    List<DayOrder> findByUserAndDateOrderByPosition(User user, LocalDateTime date);

    Optional<DayOrder> findByUserAndRoutineAndDate(User user, Routine routine, LocalDateTime date);
}

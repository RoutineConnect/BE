package com.team.routineconnect.repository;

import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourRepository extends JpaRepository<Hour, Long> {
    Optional<Hour> findByHourAndUser(String hour, User user);
}

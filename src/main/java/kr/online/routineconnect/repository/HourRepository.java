package kr.online.routineconnect.repository;

import java.util.LinkedHashSet;
import java.util.Optional;
import kr.online.routineconnect.domain.Hour;
import kr.online.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourRepository extends JpaRepository<Hour, Long> {

    Optional<Hour> findByHourAndUser(String hour, User user);

    LinkedHashSet<Hour> findByUserIsNull();
}

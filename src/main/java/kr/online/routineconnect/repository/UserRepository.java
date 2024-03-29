package kr.online.routineconnect.repository;

import java.util.Optional;
import kr.online.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

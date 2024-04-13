package kr.online.routineconnect.repository;

import java.time.LocalDate;
import java.util.Optional;
import kr.online.routineconnect.domain.Accomplishment;
import kr.online.routineconnect.domain.ItemOrder;
import kr.online.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccomplishmentRepository extends JpaRepository<Accomplishment, Long> {

    long countByUserAndDate(User user, LocalDate date);

    Optional<Accomplishment> findByItemOrder(ItemOrder itemOrder);
}

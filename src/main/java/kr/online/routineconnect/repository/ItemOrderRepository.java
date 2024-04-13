package kr.online.routineconnect.repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.online.routineconnect.domain.Item;
import kr.online.routineconnect.domain.ItemOrder;
import kr.online.routineconnect.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrderRepository extends JpaRepository<ItemOrder, Long>, ItemOrderRepositoryCustom {
    List<ItemOrder> findByUserAndDateAndItemIsNotNull(User user, LocalDate date);

    ItemOrder findTopByItemAndDayAndDateLessThanEqualOrderByDateDesc(Item item, DayOfWeek day,
                                                                     LocalDate date);

    Optional<ItemOrder> findByItemAndDate(Item item, LocalDate date);

    long countByUserAndDate(User user, LocalDate date);
}

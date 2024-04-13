package kr.online.routineconnect.repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import kr.online.routineconnect.domain.User;
import kr.online.routineconnect.dto.ItemResponse;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrderRepositoryCustom {
    
    List<ItemResponse> findItemsByUserAndDate(User user, LocalDate date);

    double findMaxPositionByUserAndDayAndDate(User user, DayOfWeek day, LocalDate date);
}

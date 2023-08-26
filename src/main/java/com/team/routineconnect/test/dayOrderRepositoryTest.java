package com.team.routineconnect.test;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import com.team.routineconnect.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class dayOrderRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoutineRepository routineRepository;
    @Autowired
    DayOrderRepository dayOrderRepository;

    @DisplayName("유저와 날짜로 순서 조회 성공")
    @Test
    public void 유저와날짜로순서조회Test() throws Exception {
        final String title="title";
        final Byte routineDay=(byte)0b11111110;
        final LocalDateTime createdDate=LocalDateTime.parse("2023-08-22T22:55:00");
        final User user=new User("홍길동","@",null);
        final Routine routine=new Routine(user,title,null,routineDay,false,createdDate,null);
        final DayOrder dayOrder=new DayOrder(user,routine,createdDate,createdDate.getDayOfWeek(),1f);
        userRepository.save(user);
        routineRepository.save(routine);
        dayOrderRepository.save(dayOrder);

        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user,createdDate);
        assertThat(dayOrders.size()).isEqualTo(1);
    }
}

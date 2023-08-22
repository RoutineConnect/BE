package com.team.routineconnect.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class RoutineServiceTest {

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    RoutineService routineService;
    @Autowired
    RoutineRepository routineRepository;
    @Autowired
    DayOrderRepository dayOrderRepository;
    @Autowired
    private WebApplicationContext context;
    private User user1;
    private Byte routineDay;
    private String hour;
    private Boolean shared;
    private LocalDateTime createdDate;

    @BeforeEach
    public void set() {
        this.user1 = new User("1");
        this.routineDay = 0b1111111;
        this.hour = "hour";
        this.shared = false;
        this.createdDate = LocalDateTime.parse("2023-08-22T22:55:00");
    }

    @DisplayName("루틴 추가 성공")
    @Test
    public void 루틴추가Test() throws Exception {
        final String title = "title";
        final Routine routine = new Routine(user1, routineDay, title, hour, shared, createdDate);

        routineService.add(routine);

        // Then
        List<Routine> routines = routineRepository.findAll();
        assertThat(routines.size()).isEqualTo(1);

        List<DayOrder> dayOrders = dayOrderRepository.findAll();
        assertThat(dayOrders.size()).isEqualTo(7);
    }

    @DisplayName("루틴이 존재 할 때 새로운 루틴 추가 성공")
    @Test
    public void 이미루틴이있을때새루틴추가Test() throws Exception {
        final String title1 = "title1";
        final String title2 = "title2";
        final LocalDateTime laterRoutineDate = LocalDateTime.now();
        final Routine routine1 = new Routine(user1, routineDay, title1, hour, shared, createdDate);
        final Routine routine2 = new Routine(user1, routineDay, title2, hour, shared, laterRoutineDate);

        routineService.add(routine1);
        routineService.add(routine2);

        // Then
        List<Routine> routines = routineRepository.findAll();
        assertThat(routines.size()).isEqualTo(2);

        List<DayOrder> allDayOrders = dayOrderRepository.findAll();
        assertThat(allDayOrders.size()).isEqualTo(14);

        List<DayOrder> dayOrder = dayOrderRepository.findByUserAndDate(user1, createdDate);
        assertThat(dayOrder.size()).isEqualTo(1);

        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user1, laterRoutineDate);
        assertThat(dayOrders.size()).isEqualTo(2);
    }

    @DisplayName("루틴 순서 변경 성공")
    @Test
    public void 루틴순서변경Test() throws Exception {
        final String title1 = "title1";
        final String title2 = "title2";
        final Float position = 0.5f;
        final Routine routine1 = new Routine(user1, routineDay, title1, hour, shared, createdDate);
        final Routine routine2 = new Routine(user1, routineDay, title2, hour, shared, createdDate);
        routineService.add(routine1);
        routineService.add(routine2);

        routineService.modifyOrder(routine2, position);

        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user1, createdDate);
        assertThat(dayOrders.get(0).getRoutine()).isEqualTo(routine2);
        assertThat(dayOrders.get(1).getRoutine()).isEqualTo(routine1);
    }
}

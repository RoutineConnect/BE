package com.team.routineconnect.test;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.service.RoutineService;
import com.team.routineconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class RoutineServiceTest {

    @Autowired
    RoutineService routineService;
    @Autowired
    UserService userService;
    @Autowired
    DayOrderRepository dayOrderRepository;
    @Autowired
    EnumSetToBitmaskConverter enumSetToBitmaskConverter;
    private User user1;
    private Byte routineDay;
    private String hour;
    private Boolean shared;
    private LocalDateTime createdDate;
    private LocalDateTime endedDate;
    private String title1 = "title1";
    private String title2 = "title2";

    @BeforeEach
    public void set() {
        userService.deleteAll();
        this.user1 = userService.save(new User("홍길동", "@", null));
        this.routineDay = (byte) 0b11111110;
        this.hour = null;
        this.shared = false;
        this.createdDate = LocalDateTime.parse("2023-08-22T22:55:00");
        this.endedDate = null;
        this.title1 = "title1";
        this.title2 = "title2";
    }

    @DisplayName("루틴 추가 성공")
    @Test
    public void 루틴추가Test() throws Exception {
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);

        routineService.save(user1.getId(), request);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(1);

        List<DayOrder> dayOrders = dayOrderRepository.findAll();
        assertThat(dayOrders.size()).isEqualTo(7);
    }

    @DisplayName("루틴이 존재 할 때 새로운 루틴 추가 성공")
    @Test
    public void 이미루틴이있을때새루틴추가Test() throws Exception {

        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);

        routineService.save(user1.getId(), request1);
        routineService.save(user1.getId(), request2);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(2);

        List<DayOrder> allDayOrders = dayOrderRepository.findAll();
        assertThat(allDayOrders.size()).isEqualTo(14);

        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user1, createdDate.with(LocalTime.MIN));
        assertThat(dayOrders.size()).isEqualTo(2);
    }

    @DisplayName("루틴이 존재 할 때 일주일 뒤 새로운 루틴 추가 성공")
    @Test
    public void 이미루틴이있을때일주일뒤새루틴추가Test() throws Exception {

        final LocalDateTime laterRoutineDate = createdDate.plusDays(7);
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, laterRoutineDate, endedDate, enumSetToBitmaskConverter);

        routineService.save(user1.getId(), request1);
        routineService.save(user1.getId(), request2);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(2);

        List<DayOrder> allDayOrders = dayOrderRepository.findAll();
        assertThat(allDayOrders.size()).isEqualTo(21);

        List<DayOrder> dayOrder = dayOrderRepository.findByUserAndDate(user1, createdDate.with(LocalTime.MIN));
        assertThat(dayOrder.size()).isEqualTo(1);

        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user1, laterRoutineDate.with(LocalTime.MIN));
        assertThat(dayOrders.size()).isEqualTo(2);
    }

    @DisplayName("루틴 순서 변경 성공")
    @Test
    public void 루틴순서변경Test() throws Exception {
        final Float position = 0.5f;
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        Routine routine1 = routineService.save(user1.getId(), request1);
        Routine routine2 = routineService.save(user1.getId(), request2);
        List<RoutineUpdate> routineUpdate = new ArrayList<>(List.of(new RoutineUpdate(routine2.getId(), position)));

        routineService.modifyOrder(user1.getId(), createdDate, routineUpdate);

        List<DayOrder> dayOrders = dayOrderRepository
                .findByUserAndDateOrderByPosition(user1, createdDate.plusDays(6).with(LocalTime.MIN));
        assertThat(dayOrders.get(0).getRoutine().getTitle()).isEqualTo(routine2.getTitle());
        assertThat(dayOrders.get(1).getRoutine().getTitle()).isEqualTo(routine1.getTitle());
    }

    @DisplayName("루틴 요일 바로 변경 성공")
    @Test
    public void 루틴요일바로변경Test() throws Exception {
        final Byte newRoutineDay = 0b111110;
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, enumSetToBitmaskConverter);
        final Routine routine1 = routineService.save(user1.getId(), request);

        routineService.edit(user1.getId(), routine1.getId(), createdDate, newRequest);

        List<DayOrder> dayOrders = dayOrderRepository.findAll();
        assertThat(dayOrders.size()).isEqualTo(5);
    }
}

package com.team.routineconnect.service;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.HourDto;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.HourRepository;
import com.team.routineconnect.repository.ItemOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
public class RoutineServiceTest {

    @Autowired
    protected RoutineService routineService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected ItemOrderRepository itemOrderRepository;
    @Autowired
    protected EnumSetToBitmaskConverter enumSetToBitmaskConverter;
    @Autowired
    protected HourRepository hourRepository;
    private User user1;
    private Byte routineDay;
    private HourDto hour;
    private Boolean shared;
    private LocalDate createdDate;
    private LocalDate endedDate;
    private String title1 = "title1";
    private String title2 = "title2";

    @BeforeEach
    public void set() {
        this.user1 = userService.save(new User("홍길동", "@", "passwd", null));
        this.routineDay = (byte) 0b11111110;
        this.hour = new HourDto("hour");
        this.shared = false;
        this.createdDate = LocalDate.parse("2023-08-22");
        this.endedDate = null;
        this.title1 = "title1";
        this.title2 = "title2";
    }

    //    updateAfterDate if findDatesByUserAndDayAndDayAfter.isEmpty
    @DisplayName("루틴 추가 성공")
    @Test
    public void 루틴추가Test() throws Exception {
        final RoutineRequest request = new RoutineRequest(
                title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);

        routineService.addRoutine(user1, request);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(1);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(7);
    }

    //    updateBeforeDate if findMaxDateByUserAndDayAndDateLessThan exists
//    updateAfterDate if findDatesByUserAndDayAndDayAfter.isEmpty
    @DisplayName("루틴이 존재 할 때 새로운 루틴 추가 성공")
    @Test
    public void 이미루틴이있을때새루틴추가Test() throws Exception {

        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, createdDate, endedDate, hourRepository);

        routineService.addRoutine(user1, request1);
        routineService.addRoutine(user1, request2);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(2);

        List<ItemOrder> allItemOrders = itemOrderRepository.findAll();
        assertThat(allItemOrders.size()).isEqualTo(14);

        List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemIsNotNull(user1, createdDate);
        assertThat(itemOrders.size()).isEqualTo(2);
    }

    //    updateBeforeDate if findMaxDateByUserAndDayAndDateLessThan exists
//    updateAfterDate if findDatesByUserAndDayAndDayAfter exists
    @DisplayName("루틴이 존재 할 때 일주일 뒤 새로운 루틴 추가 성공")
    @Test
    public void 이미루틴이있을때일주일뒤새루틴추가Test() throws Exception {

        final LocalDate laterRoutineDate = createdDate.plusDays(7);
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, laterRoutineDate, endedDate, hourRepository);

        routineService.addRoutine(user1, request1);
        routineService.addRoutine(user1, request2);

        // Then
        List<Routine> routines = routineService.findAll();
        assertThat(routines.size()).isEqualTo(2);

        List<ItemOrder> allItemOrders = itemOrderRepository.findAll();
        assertThat(allItemOrders.size()).isEqualTo(21);

        List<ItemOrder> itemOrder = itemOrderRepository.findByUserAndDateAndItemIsNotNull(user1, createdDate);
        assertThat(itemOrder.size()).isEqualTo(1);

        List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemIsNotNull(user1, laterRoutineDate);
        assertThat(itemOrders.size()).isEqualTo(2);
    }

    @DisplayName("루틴 순서 변경 성공")
    @Test
    public void 루틴순서변경Test() throws Exception {
        final Float position = 0.5f;
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        Routine routine1 = routineService.addRoutine(user1, request1);
        Routine routine2 = routineService.addRoutine(user1, request2);
        List<RoutineUpdate> routineUpdate = new ArrayList<>(List.of(new RoutineUpdate(routine2.getId(), position)));

        routineService.updateRoutineOrder(user1, createdDate, routineUpdate);

        List<ItemOrder> itemOrders = itemOrderRepository
                .findByUserAndDateOrderByPosition(user1, createdDate.plusDays(6));
        assertThat(itemOrders.get(0).getRoutine().getTitle()).isEqualTo(routine2.getTitle());
        assertThat(itemOrders.get(1).getRoutine().getTitle()).isEqualTo(routine1.getTitle());
    }

    @DisplayName("이후 날짜 루틴 순서 변경 성공")
    @Test
    public void 이후날짜루틴순서변경Test() throws Exception {
        final LocalDate laterDate = createdDate.plusDays(7);
        final Float position = 0.5f;
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        Routine routine1 = routineService.addRoutine(user1, request1);
        Routine routine2 = routineService.addRoutine(user1, request2);
        List<RoutineUpdate> routineUpdate = new ArrayList<>(List.of(new RoutineUpdate(routine2.getId(), position)));

        routineService.updateRoutineOrder(user1, laterDate, routineUpdate);

        List<ItemOrder> itemOrders = itemOrderRepository
                .findByUserAndDateOrderByPosition(user1, createdDate.plusDays(6));
        assertThat(itemOrders.get(0).getRoutine().getTitle()).isEqualTo(routine2.getTitle());
        assertThat(itemOrders.get(1).getRoutine().getTitle()).isEqualTo(routine1.getTitle());
    }


    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is same date
    @DisplayName("바로 루틴 요일 제외 성공")
    @Test
    public void 바로루틴요일제외Test() throws Exception {
        final Byte newRoutineDay = 0b111110;
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(5);
    }

    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is not same date
    @DisplayName("일주일 뒤 루틴 요일 제외 성공")
    @Test
    public void 일주일뒤루틴요일제외변경Test() throws Exception {
        final Byte newRoutineDay = 0b111110;
        final LocalDate laterRoutineDate = createdDate.plusDays(7);
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, newRoutineDay, shared, laterRoutineDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(9);
    }

    //    updateAfterDate if findDatesByUserAndDayAndDayAfter.isEmpty
    @DisplayName("바로 루틴 요일 추가 성공")
    @Test
    public void 바로루틴요일추가Test() throws Exception {
        final Byte newRoutineDay = 0b111110;
        final RoutineRequest request = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(7);
    }

    //    updateAfterDate if findDatesByUserAndDayAndDayAfter exists
    @DisplayName("일주일 전 루틴 요일 추가 성공")
    @Test
    public void 일주일전루틴요일추가Test() throws Exception {
        final Byte newRoutineDay = 0b111110;
        LocalDate earlierRoutineDate = createdDate.minusDays(7);
        final RoutineRequest request = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, routineDay, shared, earlierRoutineDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        while (earlierRoutineDate.isBefore(createdDate)) {
            assertThat(itemOrderRepository
                    .findByUserAndDateAndItemIsNotNull(user1, earlierRoutineDate)
                    .size()).isEqualTo(1);
            earlierRoutineDate = earlierRoutineDate.plusDays(1);
        }
    }

    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is same date
    //    updateAfterDate if findDatesByUserAndDayAndDayAfter.isEmpty
    @DisplayName("바로 루틴 요일 동시에 추가와 제외 성공")
    @Test
    public void 바로루틴요일동시추가제외Test() throws Exception {
        routineDay = 0b1010100;
        final Byte newRoutineDay = 0b101010;
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(3);
    }

    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is same date
    //    updateAfterDate if findDatesByUserAndDayAndDayAfter exists
    @DisplayName("일주일 뒤 루틴 추가했을 때 원래 루틴 요일 동시에 추가와 제외 성공")
    @Test
    public void 일주일뒤원래루틴요일동시추가제외Test() throws Exception {
        routineDay = 0b1010100;
        final LocalDate laterRoutineDate = createdDate.plusDays(7);
        final Byte newRoutineDay = 0b101010;
        final RoutineRequest request1 = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        final RoutineRequest request2 = new RoutineRequest(title2, hour, (byte) 0b11111110, shared, laterRoutineDate, endedDate, hourRepository);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, newRoutineDay, shared, createdDate, endedDate, hourRepository);
        final Routine routine1 = routineService.addRoutine(user1, request1);
        routineService.addRoutine(user1, request2);

        routineService.updateRoutine(user1, routine1.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findAll();
        assertThat(itemOrders.size()).isEqualTo(10);
    }
    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is not same date
    //    updateAfterDate if findDatesByUserAndDayAndDayAfter.isEmpty
    //    removeBeforeDate if findMaxDateByUserAndDayAndDateBefore is not same date
    //    updateAfterDate if findDatesByUserAndDayAndDayAfter exists

    @DisplayName("루틴 종료")
    @Test
    public void 루틴종료Test() throws Exception {
        final RoutineRequest request = new RoutineRequest(title1, hour, routineDay, shared, createdDate, endedDate, hourRepository);
        Routine routine = routineService.addRoutine(user1, request);
        endedDate = createdDate.plusDays(7);
        final RoutineRequest newRequest = new RoutineRequest(title1, hour, routineDay, shared, endedDate, endedDate, hourRepository);

        routineService.updateRoutine(user1, routine.getId(), newRequest);

        List<ItemOrder> itemOrders = itemOrderRepository.findByDateGreaterThanEqual(endedDate);
        for (ItemOrder itemOrder : itemOrders) {
            assertThat(itemOrder.getRoutine()).isNull();
        }
        assertThat(itemOrders.size()).isEqualTo(7);
    }
}

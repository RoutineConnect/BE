package com.team.routineconnect.service;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final DayOrderRepository dayOrderRepository;
    private final UserService userService;

    public Routine save(Long userId, LocalDateTime date, RoutineRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LocalDateTime currentDate = date.with(LocalTime.MIN);
        LocalDateTime lastDate = currentDate.plusDays(7);
        Byte dayOfWeekBits = request.getRoutineDay();

        Routine routine = routineRepository.save(request.toEntity(user));

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            int dayIndex = day.getValue();
            if ((dayOfWeekBits & (1 << dayIndex)) != 0) {
//                해당 요일의 가장 최근 날짜 마지막 position
                Optional<LocalDateTime> maxDateOptional = dayOrderRepository.findMaxDateByUserAndDateAndDay(user, currentDate, day);
                LocalDateTime maxDate = maxDateOptional.orElse(currentDate);
                Float position = 1f;

                if (maxDateOptional.isPresent()) {
//                    이전 기록이 오늘이면
                    if (currentDate.equals(maxDate)) {
                        position=dayOrderRepository.findPositionByUserAndDateAndDay(user,currentDate,day)+1;
                    } else {
//                        이전 기록이 있으면 이전 기록을 현재 날짜로 가져오고
                        List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user, maxDate);
                        for (DayOrder dayOrder : dayOrders) {
                            DayOrder newDayOrder = DayOrder.builder()
                                    .user(user)
                                    .routine(dayOrder.getRoutine())
                                    .date(currentDate)
                                    .day(day)
                                    .position(dayOrder.getPosition())
                                    .build();
                            dayOrderRepository.save(newDayOrder);
                        }
//                        해당 날짜 position + 1 할당
                        position = dayOrders.get(dayOrders.size() - 1).getPosition() + 1;
                    }
                }

                DayOrder dayOrder = DayOrder.builder()
                        .user(user)
                        .routine(routine)
                        .date(currentDate)
                        .day(day)
                        .position(position)
                        .build();

                dayOrderRepository.save(dayOrder);
            }

            currentDate = currentDate.plusDays(1);
        }

        return routine;
    }

    public void modifyOrder(Long userId, Long routineId, LocalDateTime date, Float position) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        DayOrder dayOrder = dayOrderRepository.findByUserAndRoutineAndDate(user, routine, date.with(LocalTime.MIN))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        dayOrder.updatePosition(position);
    }

    public void edit(Routine routine, RoutineRequest request) {
    }
}

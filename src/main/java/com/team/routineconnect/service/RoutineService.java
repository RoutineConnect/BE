package com.team.routineconnect.service;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.MIN;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final DayOrderRepository dayOrderRepository;
    private final UserService userService;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public Routine save(Long userId, RoutineRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        LocalDateTime currentDate = request.getCreatedDate();
        LocalDateTime lastDate = currentDate.plusDays(7);

        Routine routine = routineRepository.save(request.toEntity(user));

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (routine.isSetTo(day)) {
                updateBeforeDateDayOrder(user, currentDate, day);
                updateAfterDateDayOrder(user, routine, currentDate, day);
            }
            currentDate = currentDate.plusDays(1);
        }
        return routine;
    }

    public void edit(Long userId, Long routineId, LocalDateTime currentDate, RoutineRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));

        validate(user.has(routine));

        Byte originalDays = enumSetToBitmaskConverter.convertToDatabaseColumn(routine.getRepeatingDays());
        Byte bitsToModify = (byte) (originalDays ^ request.getRoutineDay());
        EnumSet<DayOfWeek> daysToModify = enumSetToBitmaskConverter.convertToEntityAttribute(bitsToModify);
        LocalDateTime lastDate = currentDate.plusDays(7);

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (daysToModify.contains(day)) {
                if (routine.isSetTo(day)) {
                    removeBeforeDateDayOrder(user, routine, currentDate, day);
                } else {
                    updateAfterDateDayOrder(user, routine, currentDate, day);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        routine.setRoutine(request);
    }

    public void modifyOrder(Long userId, LocalDateTime date, List<RoutineUpdate> routineUpdates) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        for (RoutineUpdate update : routineUpdates) {
            Routine routine = routineRepository.findById(update.getRoutineId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));
            validate(user.has(routine));

            List<DayOrder> dayOrders = dayOrderRepository
                    .findByRoutineAndDateAfterOrderByDate(routine, date.with(MIN));
            Float originalPosition = dayOrders.get(0).getPosition();

            for (DayOrder dayOrder : dayOrders) {
                if (dayOrder.positionIs(originalPosition)) {
                    dayOrder.updatePosition(update.getPosition());
                }
            }
        }
    }

    public void updateBeforeDateDayOrder(User user, LocalDateTime currentDate, DayOfWeek day) {
        currentDate = currentDate.with(MIN);
//        해당 요일의 가장 최근 날짜
        Optional<LocalDateTime> lastDateOptional = dayOrderRepository.findMaxDateByUserAndDayAndDateLessThan(user, day, currentDate);

        if (lastDateOptional.isPresent()) {
            LocalDateTime latestDate = lastDateOptional.get();
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오고
            List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user, latestDate);
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
        }
    }

    public void updateAfterDateDayOrder(User user, Routine routine, LocalDateTime date, DayOfWeek day) {
        date = date.with(MIN);
        List<LocalDateTime> dates = dayOrderRepository.findDatesByUserAndDayAndDayAfter(user, day, date);

        if (dates.isEmpty()) {
            float position = 1f;

            DayOrder dayOrder = DayOrder.builder()
                    .user(user)
                    .routine(routine)
                    .date(date)
                    .day(day)
                    .position(position)
                    .build();

            dayOrderRepository.save(dayOrder);
        }

        for (LocalDateTime dateTime : dates) {
            Float position = dayOrderRepository.findMaxPositionByUserAndDate(user, dateTime) + 1;

            DayOrder dayOrder = DayOrder.builder()
                    .user(user)
                    .routine(routine)
                    .date(dateTime)
                    .day(day)
                    .position(position)
                    .build();

            dayOrderRepository.save(dayOrder);
        }
    }

    public void removeBeforeDateDayOrder(User user, Routine routine, LocalDateTime currentDate, DayOfWeek day) {
        currentDate = currentDate.with(MIN);
//        해당 요일의 가장 최근 날짜
        Optional<LocalDateTime> lastDateOptional = dayOrderRepository.findMaxDateByUserAndDayAndDateBefore(user, day, currentDate);
        LocalDateTime latestDate = lastDateOptional.orElse(currentDate);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (currentDate.equals(latestDate)) {
                dayOrderRepository.deleteByRoutineAndDate(routine, currentDate);
            } else {
                List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDateAndRoutineNot(user, latestDate, routine);

                if (dayOrders.isEmpty()) {
                    DayOrder dayOrder = DayOrder.builder()
                            .user(user)
                            .routine(null)
                            .date(currentDate)
                            .day(day)
                            .position(0f)
                            .build();

                    dayOrderRepository.save(dayOrder);
                }

//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
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
            }
        }
    }

    public List<Routine> findAll() {
        return routineRepository.findAll();
    }

    void validate(Boolean condition) {
        if (condition) {
            throw new IllegalArgumentException("Invalid routine ID");
        }
    }
}

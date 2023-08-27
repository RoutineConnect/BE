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

import static java.time.LocalDateTime.MIN;

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
                updateBeforeDayOrder(user, routine, currentDate, day);
                updateAfterDayOrder(user, routine, currentDate, day);
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

        validateUserHasRoutine(user, routine);

        Byte originalDays = enumSetToBitmaskConverter.convertToDatabaseColumn(routine.getRepeatingDays());
        Byte bitsToModify = (byte) (originalDays ^ request.getRoutineDay());
        EnumSet<DayOfWeek> daysToModify = enumSetToBitmaskConverter.convertToEntityAttribute(bitsToModify);
        routine.setRoutine(request);
        LocalDateTime lastDate = currentDate.plusDays(7);

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (daysToModify.contains(day)) {
                if (routine.isSetTo(day)) {
                    updateAfterDayOrder(user, routine, currentDate, day);
                } else {
                    removeDayOrder(user, routine, currentDate, day);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    public void modifyOrder(Long userId, LocalDateTime date, List<RoutineUpdate> routineUpdates) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        for (RoutineUpdate update : routineUpdates) {
            Routine routine = routineRepository.findById(update.getRoutineId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));
            validateUserHasRoutine(user, routine);

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

    public void updateBeforeDayOrder(User user, Routine routine, LocalDateTime currentDate, DayOfWeek day) {
        currentDate = currentDate.with(MIN);
//        해당 요일의 가장 최근 날짜
        Optional<LocalDateTime> lastDateOptional = dayOrderRepository.findMaxDateByUserAndDayAndDateBefore(user, day, currentDate);
        LocalDateTime latestDate = lastDateOptional.orElse(currentDate);
        float position = 1f;

        if (lastDateOptional.isPresent() && !currentDate.equals(latestDate)) {
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
//                해당 날짜 position을 이전 기록의 마지막 position + 1로 할당
            position = dayOrders.get(dayOrders.size() - 1).getPosition() + 1;
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

    public void updateAfterDayOrder(User user, Routine routine, LocalDateTime date, DayOfWeek day) {
        date = date.with(MIN);
        List<LocalDateTime> dates = dayOrderRepository.findDatesByUserAndDayAndDayAfter(user, day, date);

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

    public void removeDayOrder(User user, Routine routine, LocalDateTime currentDate, DayOfWeek day) {
        currentDate = currentDate.with(MIN);
//        해당 요일의 가장 최근 날짜
        Optional<LocalDateTime> lastDateOptional = dayOrderRepository.findMaxDateByUserAndDayAndDateBefore(user, day, currentDate);
        LocalDateTime latestDate = lastDateOptional.orElse(currentDate);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (currentDate.equals(latestDate)) {
                dayOrderRepository.deleteByRoutineAndDate(routine, currentDate);
            } else {
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
                List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user, latestDate);
                for (DayOrder dayOrder : dayOrders) {
                    if (dayOrder.getRoutine() != routine) {
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
        } else {
            DayOrder dayOrder = DayOrder.builder()
                    .user(user)
                    .routine(null)
                    .date(currentDate)
                    .day(day)
                    .position(0f)
                    .build();

            dayOrderRepository.save(dayOrder);
        }
    }

    public List<Routine> findAll() {
        return routineRepository.findAll();
    }

    void validateUserHasRoutine(User user, Routine routine) {
        if (!user.has(routine)) {
            throw new IllegalArgumentException("Invalid routine ID");
        }
    }
}

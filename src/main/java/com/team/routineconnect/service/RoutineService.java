package com.team.routineconnect.service;

import com.querydsl.core.Tuple;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.dto.RoutineWithAccomplishment;
import com.team.routineconnect.mapper.ResultMapper;
import com.team.routineconnect.repository.DayOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.team.routineconnect.domain.QDayOrder.dayOrder;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final DayOrderRepository dayOrderRepository;
    private final UserService userService;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;
    private final ResultMapper resultMapper;

    public List<RoutineWithAccomplishment> findRoutinesByUserOnDate(Long userId, LocalDate date) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        List<Tuple> results = dayOrderRepository.findRoutinesByUserAndDate(user, date);

        return results.stream()
                .filter(tuple -> tuple.get(dayOrder.routine) != null)
                .map(resultMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public Routine addRoutine(Long userId, RoutineRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        LocalDate currentDate = request.getCreated_date().toLocalDate();
        LocalDate lastDate = currentDate.plusDays(7);

        Routine routine = routineRepository.save(request.toEntity(user));

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (routine.isSetTo(day)) {
                updateBeforeDateDayOrder(user, currentDate, day);
                updateTodayDayOrder(user, routine, currentDate, day);
                updateAfterDateDayOrder(user, routine, currentDate, day);
            }
            currentDate = currentDate.plusDays(1);
        }
        return routine;
    }

    public void updateRoutine(Long userId, Long routineId, RoutineRequest request) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));

        validate(user.has(routine));

        Byte originalDays = enumSetToBitmaskConverter.convertToDatabaseColumn(routine.getRepeatingDays());
        Byte bitsToModify = (byte) (originalDays ^ request.getRoutine_day());
        EnumSet<DayOfWeek> daysToModify = enumSetToBitmaskConverter.convertToEntityAttribute(bitsToModify);
        EnumSet<DayOfWeek> repeatingDays = request.routineDayToEntityAttribute();
        LocalDate currentDate = request.getCreated_date().toLocalDate();
        LocalDate lastDate = currentDate.plusDays(7);
        Optional<LocalDateTime> endDate = Optional.ofNullable(request.getEnded_date());

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            List<DayOrder> dayOrders = dayOrderRepository
                    .findByUserAndRoutineAndDayAndDateLessThanEqual(user, routine, day, currentDate);

            if (routine.isSetTo(day) && dayOrders.isEmpty()) {
                updateTodayDayOrder(user, routine, currentDate, day);
            } else if (daysToModify.contains(day) && routine.isSetTo(day)) {
                removeRoutine(user, routine, currentDate, day);
            } else if (repeatingDays.contains(day) && routine.isNotSetTo(day)) {
                updateTodayDayOrder(user, routine, currentDate, day);
                updateAfterDateDayOrder(user, routine, currentDate, day);
                dayOrderRepository.deleteByRoutineAndDayAndDateGreaterThan(routine, day, currentDate);
            }

            if (endDate.isPresent() && (
                    currentDate.isEqual(endDate.get().toLocalDate())
                            || currentDate.isAfter(endDate.get().toLocalDate()))) {
                removeRoutine(user, routine, currentDate, day);
            }

            currentDate = currentDate.plusDays(1);
        }

        routine.setRoutine(request);
    }

    public void updateRoutineOrder(Long userId, LocalDate date, List<RoutineUpdate> routineUpdates) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        for (RoutineUpdate update : routineUpdates) {
            Routine routine = routineRepository.findById(update.getRoutine_id())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));
            validate(user.has(routine));

            List<DayOrder> dayOrders = dayOrderRepository
                    .findByRoutineAndDateAfterOrderByDate(routine, date);
            Float originalPosition = dayOrders.get(0).getPosition();

            for (DayOrder dayOrder : dayOrders) {
                if (dayOrder.positionIs(originalPosition)) {
                    dayOrder.updatePositionTo(update.getPosition());
                }
            }
        }
    }

    public List<Float> getAchievementsForWeek(Long userId, LocalDate date) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(7);
        List<Float> achievements = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            achievements.add(dayOrderRepository.findAchievementByUserAndDate(user, startDate));
            startDate = startDate.plusDays(1);
        }

        return achievements;
    }

    public void updateBeforeDateDayOrder(User user, LocalDate date, DayOfWeek day) {
//        해당 요일 전의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = dayOrderRepository
                .findMaxDateByUserAndDayAndDateLessThan(user, day, date);

        if (lastDateOptional.isPresent()) {
            LocalDate latestDate = lastDateOptional.get();
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
            List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDate(user, latestDate);
            for (DayOrder dayOrder : dayOrders) {
                DayOrder newDayOrder = DayOrder.builder()
                        .user(user)
                        .routine(dayOrder.getRoutine())
                        .date(date)
                        .day(day)
                        .position(dayOrder.getPosition())
                        .build();
                dayOrderRepository.save(newDayOrder);
            }
        }
    }

    public void updateTodayDayOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        Float position = dayOrderRepository.findMaxPositionByUserAndDate(user, date)
                .orElse(0f);

        DayOrder dayOrder = DayOrder.builder()
                .user(user)
                .routine(routine)
                .date(date)
                .day(day)
                .position(position + 1)
                .build();

        dayOrderRepository.save(dayOrder);
    }

    public void updateAfterDateDayOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        List<LocalDate> afterDates = dayOrderRepository.findDatesByUserAndDayAndDateGreaterThan(user, day, date);

        for (LocalDate dateTime : afterDates) {
            float position = dayOrderRepository.findMaxPositionByUserAndDate(user, dateTime).get() + 1;

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

    public void removeRoutine(User user, Routine routine, LocalDate date, DayOfWeek day) {
//        해당 요일의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = dayOrderRepository
                .findMaxDateByUserAndDayAndDateBefore(user, day, date);
        LocalDate latestDate = lastDateOptional.orElse(date);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (date.equals(latestDate)) {
                dayOrderRepository.deleteByRoutineAndDate(routine, date);
            } else {
                List<DayOrder> dayOrders = dayOrderRepository.findByUserAndDateAndRoutineNot(user, latestDate, routine);

                if (dayOrders.isEmpty()) {
                    DayOrder dayOrder = DayOrder.builder()
                            .user(user)
                            .routine(null)
                            .date(date)
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
                            .date(date)
                            .day(day)
                            .position(dayOrder.getPosition())
                            .build();
                    dayOrderRepository.save(newDayOrder);
                }
            }
        }

        dayOrderRepository.deleteAllByRoutineAndDayAndDateGreaterThan(routine, day, date);
    }

    public List<Routine> findAll() {
        return routineRepository.findAll();
    }

    void validate(Boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Invalid Argument");
        }
    }

    public void deleteAll() {
        routineRepository.deleteAll();
    }
}

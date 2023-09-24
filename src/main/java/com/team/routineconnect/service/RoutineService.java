package com.team.routineconnect.service;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.ItemOrderRepository;
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

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ItemOrderRepository itemOrderRepository;
    private final UserService userService;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public List<ItemOrder> findRoutinesByUserOnDate(Long userId, LocalDate date) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        return itemOrderRepository.findRoutinesByUserAndDate(user, date);
    }

    public void setAccomplishment(Long userId, Long routineItemId, Accomplishment accomplishment) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        ItemOrder itemOrder = itemOrderRepository.findById(routineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine item ID"));
        validate(user.equals(itemOrder.getUser()));

        itemOrder.setAccomplishment(accomplishment);
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
                updateBeforeDateItemOrder(user, currentDate, day);
                updateTodayItemOrder(user, routine, currentDate, day);
                updateAfterDateItemOrder(user, routine, currentDate, day);
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
            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByUserAndRoutineAndDayAndDateLessThanEqual(user, routine, day, currentDate);

            if (routine.isSetTo(day) && itemOrders.isEmpty()) {
                updateTodayItemOrder(user, routine, currentDate, day);
            } else if (daysToModify.contains(day) && routine.isSetTo(day)) {
                removeRoutine(user, routine, currentDate, day);
            } else if (repeatingDays.contains(day) && routine.isNotSetTo(day)) {
                updateTodayItemOrder(user, routine, currentDate, day);
                updateAfterDateItemOrder(user, routine, currentDate, day);
                itemOrderRepository.deleteByRoutineAndDayAndDateGreaterThan(routine, day, currentDate);
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
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine item ID"));
            validate(user.has(routine));

            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByRoutineAndDateAfterOrderByDate(routine, date);
            Float originalPosition = itemOrders.get(0).getPosition();

            for (ItemOrder item : itemOrders) {
                if (item.positionIs(originalPosition)) {
                    item.updatePositionTo(update.getPosition());
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
            achievements.add(itemOrderRepository.findAchievementByUserAndDate(user, startDate));
            startDate = startDate.plusDays(1);
        }

        return achievements;
    }

    public void updateBeforeDateItemOrder(User user, LocalDate date, DayOfWeek day) {
//        해당 요일 전의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = itemOrderRepository
                .findMaxDateByUserAndDayAndDateLessThan(user, day, date);

        if (lastDateOptional.isPresent()) {
            LocalDate latestDate = lastDateOptional.get();
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
            List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDate(user, latestDate);
            for (ItemOrder itemOrder : itemOrders) {
                ItemOrder newItemOrder = ItemOrder.builder()
                        .user(user)
                        .routine(itemOrder.getRoutine())
                        .date(date)
                        .day(day)
                        .position(itemOrder.getPosition())
                        .build();
                itemOrderRepository.save(newItemOrder);
            }
        }
    }

    public void updateTodayItemOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        Float position = itemOrderRepository.findMaxPositionByUserAndDate(user, date)
                .orElse(0f);

        ItemOrder itemOrder = ItemOrder.builder()
                .user(user)
                .routine(routine)
                .date(date)
                .day(day)
                .position(position + 1)
                .build();

        itemOrderRepository.save(itemOrder);
    }

    public void updateAfterDateItemOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        List<LocalDate> afterDates = itemOrderRepository.findDatesByUserAndDayAndDateGreaterThan(user, day, date);

        for (LocalDate dateTime : afterDates) {
            float position = itemOrderRepository.findMaxPositionByUserAndDate(user, dateTime).get() + 1;

            ItemOrder itemOrder = ItemOrder.builder()
                    .user(user)
                    .routine(routine)
                    .date(dateTime)
                    .day(day)
                    .position(position)
                    .build();

            itemOrderRepository.save(itemOrder);
        }
    }

    public void removeRoutine(User user, Routine routine, LocalDate date, DayOfWeek day) {
//        해당 요일의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = itemOrderRepository
                .findMaxDateByUserAndDayAndDateBefore(user, day, date);
        LocalDate latestDate = lastDateOptional.orElse(date);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (date.equals(latestDate)) {
                itemOrderRepository.deleteByRoutineAndDate(routine, date);
            } else {
                List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndRoutineNot(user, latestDate, routine);

                if (itemOrders.isEmpty()) {
                    ItemOrder itemOrder = ItemOrder.builder()
                            .user(user)
                            .routine(null)
                            .date(date)
                            .day(day)
                            .position(0f)
                            .build();

                    itemOrderRepository.save(itemOrder);
                }

//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
                for (ItemOrder itemOrder : itemOrders) {
                    ItemOrder newItemOrder = ItemOrder.builder()
                            .user(user)
                            .routine(itemOrder.getRoutine())
                            .date(date)
                            .day(day)
                            .position(itemOrder.getPosition())
                            .build();
                    itemOrderRepository.save(newItemOrder);
                }
            }
        }

        itemOrderRepository.deleteAllByRoutineAndDayAndDateGreaterThan(routine, day, date);
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

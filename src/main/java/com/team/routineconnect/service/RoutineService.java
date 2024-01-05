package com.team.routineconnect.service;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.Item;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.ItemResponse;
import com.team.routineconnect.dto.ItemUpdate;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.repository.HourRepository;
import com.team.routineconnect.repository.ItemOrderRepository;
import com.team.routineconnect.repository.ItemRepository;
import com.team.routineconnect.repository.RoutineRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ItemRepository itemRepository;
    private final ItemOrderRepository itemOrderRepository;
    private final HourRepository hourRepository;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public List<ItemResponse> findRoutinesByUserOnDate(User user, LocalDate date) {
        return itemOrderRepository.findRoutinesByUserRoutineIsNotNullAndDate(user, date);
    }

    public void setAccomplishment(User user, Long routineItemId, Boolean accomplishment) {
        ItemOrder itemOrder = itemOrderRepository.findById(routineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item order ID"));
        validate(user.equals(itemOrder.getUser()));

        itemOrder.setAccomplishment(accomplishment);
    }

    public Routine addRoutine(User user, RoutineRequest request) {
        LocalDate currentDate = request.getCreated_date();
        LocalDate lastDate = currentDate.plusDays(7);

        Routine routine = routineRepository.save(request.toEntity(user, enumSetToBitmaskConverter));

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

    public void updateRoutine(User user, Long routineId, RoutineRequest request) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));
        validate(routine.userIs(user));

        Byte originalDays = enumSetToBitmaskConverter.convertToDatabaseColumn(routine.getRepeatingDays());
        Byte bitsToModify = (byte) (originalDays ^ Integer.parseInt(request.getRoutine_day(), 2));
        EnumSet<DayOfWeek> daysToModify = enumSetToBitmaskConverter.convertToEntityAttribute(bitsToModify);
        EnumSet<DayOfWeek> repeatingDays = request.routineDayToEntityAttribute(enumSetToBitmaskConverter);
        LocalDate currentDate = request.getCreated_date();
        LocalDate lastDate = currentDate.plusDays(7);

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByItemAndDayAndDateLessThanEqual(routine, day, currentDate);

//            생성일을 이전 날짜로 수정? 마이루틴은 이후로만 바꿀 수 있음
            if (routine.isSetTo(day) && itemOrders.isEmpty()) {
                updateTodayItemOrder(user, routine, currentDate, day);
            } else if (daysToModify.contains(day) && routine.isSetTo(day)) {
                removeRoutine(user, routine, currentDate, day);
            } else if (repeatingDays.contains(day) && routine.isNotSetTo(day)) {
                updateTodayItemOrder(user, routine, currentDate, day);
                updateAfterDateItemOrder(user, routine, currentDate, day);
                itemOrderRepository.deleteByItemAndDayAndDateGreaterThan(routine, day, currentDate);
            }

            LocalDate finalCurrentDate = currentDate;
            Optional.ofNullable(request.getEnded_date())
                    .ifPresent(endDate -> {
                        if (finalCurrentDate.isEqual(endDate) || finalCurrentDate.isAfter(endDate)) {
                            removeRoutine(user, routine, finalCurrentDate, day);
                        }
                    });

            currentDate = currentDate.plusDays(1);
        }

        routine.setRoutine(request, enumSetToBitmaskConverter);
    }

    /**
     * 해당 일자 아이템 목록만 수정하는 것이 아닌 position이 같은 아이템 목록 전체를 수정하기 때문에 ItemOrder가 아니라 Item의 id를 기준으로 position을 update함.
     *
     * @param user        @AuthenticationPrincipal로 가져온, 현재 요청한 User
     * @param date        Item position을 update할 일자
     * @param itemUpdates Item id와 update할 position이 담긴 요청 DTO List
     */
    public void updateItemOrder(User user, LocalDate date, List<ItemUpdate> itemUpdates) {
        DayOfWeek day = date.getDayOfWeek();

        for (ItemUpdate update : itemUpdates) {
            Item item = itemRepository.findById(update.getItem_id())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine item ID"));
            validate(item.userIs(user));
            Float updatePosition = update.getPosition();

            ItemOrder itemOrder = itemOrderRepository.findTopByItemAndDayAndDateLessThanOrderByDateDesc(item, day, date)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine"));
            itemOrder.updatePositionTo(updatePosition);
            Float originalPosition = itemOrder.getPosition();
            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByItemAndDayAndDateAfterOrderByDate(item, day, date);

            for (ItemOrder order : itemOrders) {
                if (order.positionIs(originalPosition)) {
                    order.updatePositionTo(updatePosition);
                }
            }
        }
    }

    public List<Float> getAchievementsForWeek(User user, LocalDate date) {
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(7);
        List<Float> achievements = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            achievements.add(itemOrderRepository.findAchievementByUserAndDate(user, startDate));
            startDate = startDate.plusDays(1);
        }

        return achievements;
    }

    public Set<Hour> getHours(User user) {
        Set<Hour> hours = hourRepository.findByUserIsNull();
        hours.addAll(user.getHours());
        return hours.stream()
                .limit(10)
                .collect(Collectors.toSet());
    }

    public void updateBeforeDateItemOrder(User user, LocalDate date, DayOfWeek day) {
//        해당 요일 전의 가장 최근 날짜
        itemOrderRepository.findMaxDateByUserAndDayAndDateBefore(user, day, date)
                .ifPresent(latestDate -> {
                    List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemIsNotNull(user,
                            latestDate);
                    for (ItemOrder itemOrder : itemOrders) {
                        ItemOrder newItemOrder = ItemOrder.builder()
                                .user(user)
                                .item(itemOrder.getItem())
                                .date(date)
                                .day(day)
                                .position(itemOrder.getPosition())
                                .accomplishment(itemOrder.getAccomplishment())
                                .build();
                        itemOrderRepository.save(newItemOrder);
                    }
                });
    }

    public void updateTodayItemOrder(User user, Item item, LocalDate date, DayOfWeek day) {
        Float position = itemOrderRepository.findMaxPositionByUserAndDayAndDate(user, day, date)
                .orElse(0f);

        ItemOrder itemOrder = ItemOrder.builder()
                .user(user)
                .item(item)
                .date(date)
                .day(day)
                .position(position + 1)
                .accomplishment(false)
                .build();

        itemOrderRepository.save(itemOrder);
    }

    public void updateAfterDateItemOrder(User user, Item item, LocalDate date, DayOfWeek day) {
        List<LocalDate> afterDates = itemOrderRepository.findDatesByUserAndDayAndDateAfter(user, day, date);

        for (LocalDate dateTime : afterDates) {
            float position = itemOrderRepository.findMaxPositionByUserAndDayAndDate(user, day, dateTime).get() + 1;

            ItemOrder itemOrder = ItemOrder.builder()
                    .user(user)
                    .item(item)
                    .date(dateTime)
                    .day(day)
                    .position(position)
                    .accomplishment(false)
                    .build();

            itemOrderRepository.save(itemOrder);
        }
    }

    public void removeRoutine(User user, Routine routine, LocalDate date, DayOfWeek day) {
//        해당 요일의 가장 최근 날짜
        itemOrderRepository.findMaxDateByUserAndDayAndDateLessThan(user, day, date)
                .ifPresent(latestDate -> {
                    if (latestDate.isEqual(date)) {
                        itemOrderRepository.deleteByItemAndDate(routine, date);
                    } else {
                        List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemNot(user, latestDate,
                                routine);

                        if (itemOrders.isEmpty()) {
                            ItemOrder itemOrder = ItemOrder.builder()
                                    .user(user)
                                    .item(null)
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
                                    .item(itemOrder.getItem())
                                    .date(date)
                                    .day(day)
                                    .position(itemOrder.getPosition())
                                    .accomplishment(itemOrder.getAccomplishment())
                                    .build();
                            itemOrderRepository.save(newItemOrder);
                        }
                    }
                });

        itemOrderRepository.deleteAllByItemAndDayAndDateGreaterThan(routine, day, date);
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

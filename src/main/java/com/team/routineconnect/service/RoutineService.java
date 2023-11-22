package com.team.routineconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.*;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.HourRepository;
import com.team.routineconnect.repository.ItemOrderRepository;
import com.team.routineconnect.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ItemOrderRepository itemOrderRepository;
    private final HourRepository hourRepository;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;
    private final ObjectMapper objectMapper;

    public List<ItemOrder> findRoutinesByUserOnDate(User user, LocalDate date) {
        return itemOrderRepository.findRoutinesByUserRoutineIsNotNullAndDateLessThanEqual(user, date);
    }

    public void setAccomplishment(User user, Long routineItemId, Accomplishment accomplishment) {
        ItemOrder itemOrder = itemOrderRepository.findById(routineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item order ID"));
        validate(user.equals(itemOrder.getUser()));

        itemOrder.setAccomplishment(accomplishment);
    }

    public Item addRoutine(User user, RoutineRequest request) {
        LocalDate currentDate = request.getCreated_date();
        LocalDate lastDate = currentDate.plusDays(7);

        Item item = routineRepository.save(
                request.toEntity(user, enumSetToBitmaskConverter, objectMapper, hourRepository));

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (item.isSetTo(day)) {
                updateBeforeDateItemOrder(user, currentDate, day);
                updateTodayItemOrder(user, item, currentDate, day);
                updateAfterDateItemOrder(user, item, currentDate, day);
            }
            currentDate = currentDate.plusDays(1);
        }

        return item;
    }

    public void updateRoutine(User user, Long routineId, RoutineRequest request) {
        Item item = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine ID"));
        validate(item.userIs(user));

        Byte originalDays = enumSetToBitmaskConverter.convertToDatabaseColumn(item.getRepeatingDays());
        Byte bitsToModify = (byte) (originalDays ^ request.getRoutine_day());
        EnumSet<DayOfWeek> daysToModify = enumSetToBitmaskConverter.convertToEntityAttribute(bitsToModify);
        EnumSet<DayOfWeek> repeatingDays = request.routineDayToEntityAttribute(enumSetToBitmaskConverter);
        LocalDate currentDate = request.getCreated_date();
        LocalDate lastDate = currentDate.plusDays(7);
        Optional<LocalDate> endDate = Optional.ofNullable(request.getEnded_date());

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByItemAndDayAndDateLessThanEqual(item, day, currentDate);

//            생성일을 이전 날짜로 수정? 마이루틴은 이후로만 바꿀 수 있음
            if (item.isSetTo(day) && itemOrders.isEmpty()) {
                updateTodayItemOrder(user, item, currentDate, day);
            } else if (daysToModify.contains(day) && item.isSetTo(day)) {
                removeRoutine(user, item, currentDate, day);
            } else if (repeatingDays.contains(day) && item.isNotSetTo(day)) {
                updateTodayItemOrder(user, item, currentDate, day);
                updateAfterDateItemOrder(user, item, currentDate, day);
                itemOrderRepository.deleteByItemAndDayAndDateGreaterThan(item, day, currentDate);
            }

            if (endDate.isPresent() && (
                    currentDate.isEqual(endDate.get())
                            || currentDate.isAfter(endDate.get()))) {
                removeRoutine(user, item, currentDate, day);
            }

            currentDate = currentDate.plusDays(1);
        }

        item.setRoutine(request, enumSetToBitmaskConverter, objectMapper);
    }

    /**
     * 해당 일자 아이템 목록만 수정하는 것이 아닌 position이 같은 아이템 목록 전체를 수정하기 때문에 ItemOrder가 아니라 Item의 id를
     * 기준으로 position을 update함.
     *
     * @param user @AuthenticationPrincipal로 가져온, 현재 요청한 User
     * @param date Item position을 update할 일자
     * @param routineUpdates Item id와 update할 position이 담긴 요청 DTO List
     */
    public void updateRoutineOrder(User user, LocalDate date, List<RoutineUpdate> routineUpdates) {
        for (RoutineUpdate update : routineUpdates) {
            Item routine = routineRepository.findById(update.getRoutine_id())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid routine item ID"));
            validate(routine.userIs(user));
            Float updatePosition= update.getPosition();

            ItemOrder itemOrder=itemOrderRepository.findTopByItemAndDateLessThanOrderByDateDesc(routine, date)
                    .orElseThrow(()->new IllegalArgumentException("Invalid routine"));
            itemOrder.updatePositionTo(updatePosition);
            Float originalPosition = itemOrder.getPosition();
            List<ItemOrder> itemOrders = itemOrderRepository
                    .findByItemAndDateAfterOrderByDate(routine, date);

            for (ItemOrder item : itemOrders) {
                if (item.positionIs(originalPosition)) {
                    item.updatePositionTo(updatePosition);
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

    public void updateBeforeDateItemOrder(User user, LocalDate date, DayOfWeek day) {
//        해당 요일 전의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = itemOrderRepository
                .findMaxDateByUserAndDayAndDateLessThan(user, day, date);

        if (lastDateOptional.isPresent()) {
            LocalDate latestDate = lastDateOptional.get();
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
            List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemIsNotNull(user, latestDate);
            for (ItemOrder itemOrder : itemOrders) {
                ItemOrder newItemOrder = ItemOrder.builder()
                        .user(user)
                        .item(itemOrder.getItem())
                        .date(date)
                        .day(day)
                        .position(itemOrder.getPosition())
                        .build();
                itemOrderRepository.save(newItemOrder);
            }
        }
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
                    .build();

            itemOrderRepository.save(itemOrder);
        }
    }

    public void removeRoutine(User user, Item item, LocalDate date, DayOfWeek day) {
//        해당 요일의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = itemOrderRepository
                .findMaxDateByUserAndDayAndDateBefore(user, day, date);
        LocalDate latestDate = lastDateOptional.orElse(date);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (date.equals(latestDate)) {
                itemOrderRepository.deleteByItemAndDate(item, date);
            } else {
                List<ItemOrder> itemOrders = itemOrderRepository.findByUserAndDateAndItemNot(user, latestDate, item);

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
                            .build();
                    itemOrderRepository.save(newItemOrder);
                }
            }
        }

        itemOrderRepository.deleteAllByItemAndDayAndDateGreaterThan(item, day, date);
    }

    public List<Item> findAll() {
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

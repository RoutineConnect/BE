package com.team.routineconnect.service;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.RoutineItem;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.repository.RoutineItemRepository;
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
    private final RoutineItemRepository routineItemRepository;
    private final UserService userService;
    private final EnumSetToBitmaskConverter enumSetToBitmaskConverter;

    public List<RoutineItem> findRoutinesByUserOnDate(Long userId, LocalDate date) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        return routineItemRepository.findRoutinesByUserAndDate(user, date);
    }

    public void setAccomplishment(Long userId, Long routineItemId, Accomplishment accomplishment) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        RoutineItem routineItem = routineItemRepository.findById(routineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid routine item ID"));
        validate(user.equals(routineItem.getUser()));

        routineItem.setAccomplishment(accomplishment);
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
            List<RoutineItem> routineItems = routineItemRepository
                    .findByUserAndRoutineAndDayAndDateLessThanEqual(user, routine, day, currentDate);

            if (routine.isSetTo(day) && routineItems.isEmpty()) {
                updateTodayDayOrder(user, routine, currentDate, day);
            } else if (daysToModify.contains(day) && routine.isSetTo(day)) {
                removeRoutine(user, routine, currentDate, day);
            } else if (repeatingDays.contains(day) && routine.isNotSetTo(day)) {
                updateTodayDayOrder(user, routine, currentDate, day);
                updateAfterDateDayOrder(user, routine, currentDate, day);
                routineItemRepository.deleteByRoutineAndDayAndDateGreaterThan(routine, day, currentDate);
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

            List<RoutineItem> routineItems = routineItemRepository
                    .findByRoutineAndDateAfterOrderByDate(routine, date);
            Float originalPosition = routineItems.get(0).getPosition();

            for (RoutineItem item : routineItems) {
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
            achievements.add(routineItemRepository.findAchievementByUserAndDate(user, startDate));
            startDate = startDate.plusDays(1);
        }

        return achievements;
    }

    public void updateBeforeDateDayOrder(User user, LocalDate date, DayOfWeek day) {
//        해당 요일 전의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = routineItemRepository
                .findMaxDateByUserAndDayAndDateLessThan(user, day, date);

        if (lastDateOptional.isPresent()) {
            LocalDate latestDate = lastDateOptional.get();
//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
            List<RoutineItem> routineItems = routineItemRepository.findByUserAndDate(user, latestDate);
            for (RoutineItem routineItem : routineItems) {
                RoutineItem newRoutineItem = RoutineItem.builder()
                        .user(user)
                        .routine(routineItem.getRoutine())
                        .date(date)
                        .day(day)
                        .position(routineItem.getPosition())
                        .build();
                routineItemRepository.save(newRoutineItem);
            }
        }
    }

    public void updateTodayDayOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        Float position = routineItemRepository.findMaxPositionByUserAndDate(user, date)
                .orElse(0f);

        RoutineItem routineItem = RoutineItem.builder()
                .user(user)
                .routine(routine)
                .date(date)
                .day(day)
                .position(position + 1)
                .build();

        routineItemRepository.save(routineItem);
    }

    public void updateAfterDateDayOrder(User user, Routine routine, LocalDate date, DayOfWeek day) {
        List<LocalDate> afterDates = routineItemRepository.findDatesByUserAndDayAndDateGreaterThan(user, day, date);

        for (LocalDate dateTime : afterDates) {
            float position = routineItemRepository.findMaxPositionByUserAndDate(user, dateTime).get() + 1;

            RoutineItem routineItem = RoutineItem.builder()
                    .user(user)
                    .routine(routine)
                    .date(dateTime)
                    .day(day)
                    .position(position)
                    .build();

            routineItemRepository.save(routineItem);
        }
    }

    public void removeRoutine(User user, Routine routine, LocalDate date, DayOfWeek day) {
//        해당 요일의 가장 최근 날짜
        Optional<LocalDate> lastDateOptional = routineItemRepository
                .findMaxDateByUserAndDayAndDateBefore(user, day, date);
        LocalDate latestDate = lastDateOptional.orElse(date);

        if (lastDateOptional.isPresent()) {
//            이전 기록이 오늘이면
            if (date.equals(latestDate)) {
                routineItemRepository.deleteByRoutineAndDate(routine, date);
            } else {
                List<RoutineItem> routineItems = routineItemRepository.findByUserAndDateAndRoutineNot(user, latestDate, routine);

                if (routineItems.isEmpty()) {
                    RoutineItem routineItem = RoutineItem.builder()
                            .user(user)
                            .routine(null)
                            .date(date)
                            .day(day)
                            .position(0f)
                            .build();

                    routineItemRepository.save(routineItem);
                }

//                이전 기록이 있으면 이전 기록을 현재 날짜로 가져오기
                for (RoutineItem routineItem : routineItems) {
                    RoutineItem newRoutineItem = RoutineItem.builder()
                            .user(user)
                            .routine(routineItem.getRoutine())
                            .date(date)
                            .day(day)
                            .position(routineItem.getPosition())
                            .build();
                    routineItemRepository.save(newRoutineItem);
                }
            }
        }

        routineItemRepository.deleteAllByRoutineAndDayAndDateGreaterThan(routine, day, date);
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

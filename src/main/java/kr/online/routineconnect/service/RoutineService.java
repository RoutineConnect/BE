package kr.online.routineconnect.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.online.routineconnect.domain.Accomplishment;
import kr.online.routineconnect.domain.CustomUserDetails;
import kr.online.routineconnect.domain.Hour;
import kr.online.routineconnect.domain.ItemOrder;
import kr.online.routineconnect.domain.Routine;
import kr.online.routineconnect.dto.ItemResponse;
import kr.online.routineconnect.dto.ItemUpdate;
import kr.online.routineconnect.dto.RoutineRequest;
import kr.online.routineconnect.mapper.ItemOrderMapper;
import kr.online.routineconnect.mapper.RoutineMapper;
import kr.online.routineconnect.repository.AccomplishmentRepository;
import kr.online.routineconnect.repository.HourRepository;
import kr.online.routineconnect.repository.ItemOrderIgnoreRepository;
import kr.online.routineconnect.repository.ItemOrderRepository;
import kr.online.routineconnect.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ItemOrderRepository itemOrderRepository;
    private final HourRepository hourRepository;
    private final ItemOrderIgnoreRepository itemOrderIgnoreRepository;
    private final AccomplishmentRepository accomplishmentRepository;
    private final RoutineMapper mapper;

    @Transactional(readOnly = true)
    public List<ItemResponse> findItemsByUserOnDate(CustomUserDetails userDetails, LocalDate date) {
        return itemOrderRepository.findItemsByUserAndDate(userDetails.getUser(), date);
    }

    public void setAccomplishment(CustomUserDetails userDetails, Long itemOrderId, Boolean accomplishment)
            throws IllegalArgumentException {
        var user = userDetails.getUser();
        ItemOrder itemOrder = itemOrderRepository.findById(itemOrderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 ItemOrder ID 입니다."));
        validate(user.equals(itemOrder.getUser()));

        accomplishmentRepository.findByItemOrder(itemOrder)
                .ifPresentOrElse(
                        accomplish -> accomplish.setAccomplishment(accomplishment),
                        () -> accomplishmentRepository.save(Accomplishment.builder()
                                .user(user)
                                .itemOrder(itemOrder)
                                .date(itemOrder.getDate())
                                .accomplishment(accomplishment)
                                .build())
                );
    }

    public Routine addRoutine(CustomUserDetails userDetails, RoutineRequest request) {
        var user = userDetails.getUser();
        LocalDate currentDate = request.getCreatedDate();
        LocalDate lastDate = currentDate.plusWeeks(1);
        Routine routine = routineRepository.save(mapper.requestToRoutine(request, user));

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (routine.isSetOn(day)) {
                double position = itemOrderRepository.findMaxPositionByUserAndDayAndDate(user, day, currentDate);
                itemOrderRepository.save(ItemOrder.builder()
                        .user(user)
                        .item(routine)
                        .date(currentDate)
                        .day(day)
                        .position(position != 0 ? position : 1)
                        .build());
            }
            currentDate = currentDate.plusDays(1);
        }

        return routine;
    }

    public void updateRoutine(CustomUserDetails userDetails, Long routineId, RoutineRequest request)
            throws IllegalArgumentException {
        var user = userDetails.getUser();
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 Routine ID 입니다."));
        validate(routine.userIs(user));

        var requestRoutine = mapper.requestToRoutine(request, user);
        EnumSet<DayOfWeek> repeatingDays = requestRoutine.getRepeatingDays();
        LocalDate currentDate = request.getCreatedDate();
        LocalDate lastDate = currentDate.plusWeeks(1);
        LocalDate endDate = request.getEndedDate();

        while (currentDate.isBefore(lastDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();

            if (!repeatingDays.contains(day) && routine.isSetOn(day)) {
                removeItemOrder(userDetails, routineId, currentDate);
            }

            if (repeatingDays.contains(day) && !routine.isSetOn(day)) {
                itemOrderRepository.save(ItemOrder.builder()
                        .user(user)
                        .item(routine)
                        .date(currentDate)
                        .day(day)
                        .position(itemOrderRepository.findMaxPositionByUserAndDayAndDate(user, day, currentDate))
                        .build());
            }

            if (endDate != null && ((currentDate.isEqual(endDate)) || currentDate.isAfter(endDate))) {
                removeItemOrder(userDetails, routineId, currentDate);
            }

            currentDate = currentDate.plusDays(1);
        }

        mapper.updateRoutineFromRequest(routine, request);
    }

    public void updateItemOrder(CustomUserDetails userDetails, LocalDate date, List<ItemUpdate> itemUpdates)
            throws IllegalArgumentException {
        var user = userDetails.getUser();
        DayOfWeek day = date.getDayOfWeek();

        for (ItemUpdate update : itemUpdates) {
            ItemOrder itemOrder = itemOrderRepository.findById(update.getItemOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("잘못된 ItemOrder ID 입니다."));
            validate(itemOrder.userIs(user));
            Double newPosition = update.getPosition();

            if (itemOrder.getDate().isEqual(date)) {
                itemOrder.updatePositionTo(newPosition);
            } else {
                itemOrderIgnoreRepository.save(ItemOrderMapper.INSTANCE.toIgnore(itemOrder));
                itemOrderRepository.save(ItemOrder.builder()
                        .user(user)
                        .item(itemOrder.getItem())
                        .date(date)
                        .day(day)
                        .position(newPosition)
                        .build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Float> getAchievementsForWeek(CustomUserDetails userDetails, LocalDate date) {
        var user = userDetails.getUser();
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusWeeks(1);
        List<Float> achievements = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            var totalItemOrders = itemOrderRepository.countByUserAndDate(user, date);
            var accomplishments = accomplishmentRepository.countByUserAndDate(user, endDate);

            achievements.add(totalItemOrders != 0 ? accomplishments / totalItemOrders : 0f);
            startDate = startDate.plusDays(1);
        }

        return achievements;
    }

    @Transactional(readOnly = true)
    public Set<Hour> getHours(CustomUserDetails userDetails) {
        Set<Hour> hours = hourRepository.findByUserIsNull();
        hours.addAll(userDetails.getUser().getHours());
        return hours.stream()
                .limit(Hour.MAX_HOURS)
                .collect(Collectors.toSet());
    }

    public void removeItemOrder(CustomUserDetails userDetails, Long routineId, LocalDate date) {
        var user = userDetails.getUser();
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 Routine ID 입니다."));
        validate(routine.userIs(user));

        itemOrderRepository.findByItemAndDate(routine, date)
                .ifPresentOrElse(
                        // date에 저장돼있다면 제거
                        itemOrder -> itemOrderRepository.deleteById(itemOrder.getId()),
                        // 아니라면 itemOrderIgnore에 저장
                        () -> {
                            var day = date.getDayOfWeek();
                            var itemOrder = itemOrderRepository
                                    .findTopByItemAndDayAndDateLessThanEqualOrderByDateDesc(routine, day, date);
                            itemOrderIgnoreRepository.save(ItemOrderMapper.INSTANCE.toIgnore(itemOrder));
                        });
    }

    public void removeRoutine(CustomUserDetails userDetails, Long routineId) {
        var user = userDetails.getUser();
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 Routine ID 입니다."));
        validate(routine.userIs(user));

        routineRepository.deleteById(routineId);
    }

    private void validate(Boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("잘못된 값입니다.");
        }
    }
}

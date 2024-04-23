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
import kr.online.routineconnect.domain.Item;
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

}

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

}

package kr.online.routineconnect.mapper;

import java.time.DayOfWeek;
import java.util.EnumSet;
import kr.online.routineconnect.converter.EnumSetToBitmaskConverter;
import kr.online.routineconnect.domain.Hour;
import kr.online.routineconnect.domain.Routine;
import kr.online.routineconnect.domain.User;
import kr.online.routineconnect.dto.RoutineRequest;
import kr.online.routineconnect.repository.HourRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class RoutineMapper {

    @Autowired
    protected EnumSetToBitmaskConverter enumSetToBitmaskConverter;
    @Autowired
    protected HourRepository hourRepository;

    @Mapping(target = "user", expression = "java( user )")
    @Mapping(target = "repeatingDays", source = "routineRequest.routineDay")
    @Mapping(target = "hour", source = "routineRequest.hour", qualifiedByName = "setHourWith")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endedDate", ignore = true)
    public abstract Routine requestToRoutine(RoutineRequest routineRequest, @Context User user);

    @Mapping(target = "repeatingDays", source = "routineDay")
    @Mapping(target = "hour", expression = "java( setHourWith( routineRequest.getHour(), routine.getUser() ) )")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endedDate", ignore = true)
    public abstract void updateRoutineFromRequest(@MappingTarget Routine routine, RoutineRequest routineRequest);

    @Named("setHourWith")
    protected Hour setHourWith(String hour, @Context User user) {
        return hour != null && hour.isEmpty() ?
                hourRepository.findByHourAndUser(hour, user)
                        .orElseGet(() -> hourRepository.save(
                                Hour.builder()
                                        .hour(hour)
                                        .user(user)
                                        .build()
                        ))
                : null;
    }

    protected EnumSet<DayOfWeek> map(Byte routineDay) {
        return enumSetToBitmaskConverter.convertToEntityAttribute(routineDay);
    }
}

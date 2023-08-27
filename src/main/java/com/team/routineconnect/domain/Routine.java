package com.team.routineconnect.domain;

import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.dto.RoutineRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Routine {

    @OneToMany(mappedBy = "routine", orphanRemoval = true)
    private final List<DayOrder> dayOrderList = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    //    일월화수목금토
//    ________ 0 or 1
    @Column(nullable = false)
    private String title;
    @Column
    private String hour;
    @Convert(converter = EnumSetToBitmaskConverter.class)
    @Column(nullable = false)
    private EnumSet<DayOfWeek> repeatingDays;
    @Column(nullable = false)
    private Boolean shared;
    @Column(nullable = false)
    private LocalDateTime createdDate;
    @Column
    private LocalDateTime endedDate;

    @Builder
    public Routine(User user, String title, String hour, EnumSet<DayOfWeek> repeatingDays, Boolean shared, LocalDateTime createdDate, LocalDateTime endedDate) {
        this.user = user;
        this.title = title;
        this.hour = hour;
        this.repeatingDays = repeatingDays;
        this.shared = shared;
        this.createdDate = createdDate;
        this.endedDate = endedDate;
    }

    public Boolean isSetTo(Object o) {
        return repeatingDays.contains(o);
    }

    public void setRoutine(RoutineRequest request) {
        this.title=request.getTitle();
        this.hour=request.getHour();
        this.repeatingDays=request.routineDayToEntityAttribute();
        this.shared=request.getShared();
        this.createdDate=request.getCreatedDate();
        this.endedDate=request.getEndedDate();
    }
}

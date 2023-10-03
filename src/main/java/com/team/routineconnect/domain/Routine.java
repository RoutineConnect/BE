package com.team.routineconnect.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.dto.RoutineRequest;
import lombok.*;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Routine {

    @OneToOne
    @JoinColumn(name = "hour_id")
    private Hour hour;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    //    일월화수목금토
//    ________ 0 or 1
    @Column(nullable = false)
    private String title;
    @Convert(converter = EnumSetToBitmaskConverter.class)
    @Column(nullable = false)
    private EnumSet<DayOfWeek> repeatingDays;
    @Column(nullable = false)
    private Boolean shared;
    @Column(nullable = false)
    private LocalDateTime createdDate;
    @Column
    private LocalDateTime endedDate;

    public Boolean isSetTo(Object o) {
        return repeatingDays.contains(o);
    }

    public Boolean isNotSetTo(Object o) {
        return !repeatingDays.contains(o);
    }

    public Boolean userIs(User user) {
        return this.user == user;
    }

    public void setRoutine(RoutineRequest request) {
        this.title = request.getTitle();
        this.hour = request.getHour();
        this.repeatingDays = request.routineDayToEntityAttribute();
        this.shared = request.getShared();
        this.createdDate = request.getCreated_date();
        this.endedDate = request.getEnded_date();
    }
}

package com.team.routineconnect.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
import com.team.routineconnect.dto.RoutineRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Routine.class, name = "routine"),
        // 다른 서브 타입들 추가
})
@DiscriminatorColumn(name = "type")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Item {

    @OneToOne
    @JoinColumn(name = "hour_id")
    private Hour hour;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String title;
    //    일월화수목금토
//    ________ 0 or 1
    @Convert(converter = EnumSetToBitmaskConverter.class)
    @Column(nullable = false)
    private EnumSet<DayOfWeek> repeatingDays;
    @Column(nullable = false)
    private Boolean shared;
    @Column(nullable = false)
    private LocalDate createdDate;
    @Column
    private LocalDate endedDate;


    public Item(User user, String title, Hour hour, EnumSet<DayOfWeek> repeatingDays, Boolean shared,
                LocalDate createdDate, LocalDate endedDate) {
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

    public Boolean isNotSetTo(Object o) {
        return !repeatingDays.contains(o);
    }

    public Boolean userIs(User user) {
        return this.user == user;
    }

    public void setRoutine(RoutineRequest request,
                           EnumSetToBitmaskConverter enumSetToBitmaskConverter, ObjectMapper objectMapper) {
        this.title = request.getTitle();
        this.hour = request.getHour().toEntity(objectMapper);
        this.repeatingDays = request.routineDayToEntityAttribute(enumSetToBitmaskConverter);
        this.shared = request.getShared();
        this.createdDate = request.getCreated_date();
        this.endedDate = request.getEnded_date();
    }
}
package com.team.routineconnect.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.team.routineconnect.converter.EnumSetToBitmaskConverter;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder
@Getter
@Entity
public class Item {

    @OneToOne
    @JoinColumn(name = "hour_id")
    protected Hour hour;
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;
    @Column(nullable = false)
    protected String title;
    //    일월화수목금토
//    ________ 0 or 1
    @Convert(converter = EnumSetToBitmaskConverter.class)
    @Column(nullable = false)
    protected EnumSet<DayOfWeek> repeatingDays;
    @Column(nullable = false)
    protected Boolean shared;
    @Column(nullable = false)
    protected LocalDate createdDate;
    @Column
    protected LocalDate endedDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    public Boolean isSetTo(Object o) {
        return repeatingDays.contains(o);
    }

    public Boolean isNotSetTo(Object o) {
        return !repeatingDays.contains(o);
    }

    public Boolean userIs(User user) {
        return this.user == user;
    }
}
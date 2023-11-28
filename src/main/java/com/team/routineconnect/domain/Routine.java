package com.team.routineconnect.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Entity;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;


@OnDelete(action = OnDeleteAction.CASCADE)
@JsonTypeName("routine")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Routine extends Item {
    public Routine(User user, String title, Hour hour, EnumSet<DayOfWeek> repeatingDays, Boolean shared,
                   LocalDate createdDate, LocalDate endedDate, String retrospective) {
        super(user, title, hour, repeatingDays, shared, createdDate, endedDate, retrospective);
    }
}

package com.team.routineconnect.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Routine extends Item {
    public Routine(User user, String title, Hour hour, EnumSet<DayOfWeek> repeatingDays, Boolean shared,
                   LocalDate createdDate, LocalDate endedDate,String retrospective) {
        super(user,title,hour,repeatingDays,shared,createdDate,endedDate,retrospective);
    }
}

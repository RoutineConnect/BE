package com.team.routineconnect.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class DayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    private Routine routine;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    @Column(nullable = false)
    private Float position;
    @Enumerated(EnumType.STRING)
    private Accomplishment accomplishment;

    @Builder
    public DayOrder(User user, Routine routine, LocalDate date, DayOfWeek day, Float position) {
        this.user = user;
        this.routine = routine;
        this.date = date;
        this.day = day;
        this.position = position;
    }

    public Boolean positionIs(Float position) {
        return this.position.equals(position);
    }

    public void updatePositionTo(Float newPosition) {
        this.position = newPosition;
    }

    public void updateAchievement(Accomplishment accomplishment) {
        this.accomplishment = accomplishment;
    }
}
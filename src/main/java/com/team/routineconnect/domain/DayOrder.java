package com.team.routineconnect.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Getter
@Entity
public class DayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    @Column(nullable = false)
    private Float position;
}


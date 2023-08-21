package com.team.routineconnect.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name="routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private DayType day;

    @Column(nullable = false)
    private Float position;
}


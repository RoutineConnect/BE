package com.team.routineconnect.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "DAY_ORDERS")
@Entity
public class DayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name="ROUTINE_ID", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private DayType day;

    @Column(nullable = false)
    private float position;
}


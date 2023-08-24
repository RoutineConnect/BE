package com.team.routineconnect.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Column(nullable = false)
    private Byte routineDay;
    @Column(nullable = false)
    private Boolean shared;
    @Column(nullable = false)
    private LocalDateTime createdDate;
    @Column
    private LocalDateTime endedDate;

    @Builder
    public Routine(User user, String title, String hour, Byte routineDay, Boolean shared, LocalDateTime createdDate, LocalDateTime endedDate) {
        this.user = user;
        this.title = title;
        this.hour = hour;
        this.routineDay = routineDay;
        this.shared = shared;
        this.createdDate = createdDate;
        this.endedDate = endedDate;
    }
}

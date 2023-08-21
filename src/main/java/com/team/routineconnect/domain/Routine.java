package com.team.routineconnect.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "ROUTINES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name="USER_ID", nullable = false)
    private User user;

//    일월화수목금토
//    ________ 0 or 1
    @Column(nullable = false)
    private byte routineDay;

    @Column(nullable = false)
    private String title;

    @Column
    private String hour;

    @Column(nullable = false)
    private boolean shared;

    @OneToMany(mappedBy = "routine")
    @JoinColumn
    private List<DayOrder> dayOrderList=new ArrayList<>();

    @Builder
    public Routine(User user, byte routineDay, String title, boolean shared) {
        this.user = user;
        this.routineDay = routineDay;
        this.title = title;
        this.shared = shared;
    }
}

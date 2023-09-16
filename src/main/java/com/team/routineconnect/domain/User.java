package com.team.routineconnect.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User {

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Routine> routines = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<DayOrder> dayOrders = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column
    private String profile;

    @Builder
    public User(String name, String email, String profile) {
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    public Boolean has(Routine routine) {
        return this.routines.contains(routine);
    }
}
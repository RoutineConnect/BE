package com.team.routineconnect.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.routineconnect.domain.Routine;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class RoutineRepositoryImpl implements RoutineRepositoryCustom {

    private final JPAQueryFactory queryFactory;

}

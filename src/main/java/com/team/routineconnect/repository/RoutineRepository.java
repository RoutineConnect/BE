package com.team.routineconnect.repository;

import com.team.routineconnect.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineRepository extends JpaRepository<Item, Long> {

}

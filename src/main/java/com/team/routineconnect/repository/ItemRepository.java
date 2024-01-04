package com.team.routineconnect.repository;

import com.team.routineconnect.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

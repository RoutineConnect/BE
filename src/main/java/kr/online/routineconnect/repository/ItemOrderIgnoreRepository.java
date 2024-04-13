package kr.online.routineconnect.repository;

import kr.online.routineconnect.domain.ItemOrderIgnore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrderIgnoreRepository extends JpaRepository<ItemOrderIgnore, Long> {
}

package com.team.routineconnect.test;

import com.team.routineconnect.domain.DayOrder;
import com.team.routineconnect.domain.Routine;
import com.team.routineconnect.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
public class EntityTest {
    @Autowired
    EntityManager em;

    @DisplayName("루틴이 있는 유저 제거")
    @Test
    public void 루틴있는유저제거Test() throws Exception {
        LocalDateTime now=LocalDateTime.now();
        User user = new User("홍길동", "@", null);
        Routine routine = new Routine(
                user,
                "title",
                null,
                EnumSet.noneOf(DayOfWeek.class),
                false,
                now,
                null
        );
        DayOrder dayOrder=new DayOrder(user,routine,now.toLocalDate(),now.getDayOfWeek(),0f);
        em.persist(user);
        em.persist(routine);
        em.persist(dayOrder);
        em.flush();
        assertThat(em.find(User.class, 1L)).isEqualTo(user);

        em.remove(user);

        assertThat(em.find(User.class, 1L)).isNull();
    }
}

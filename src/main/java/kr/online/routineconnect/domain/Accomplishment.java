package kr.online.routineconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Accomplishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    @JoinColumn(name = "item_order_id", nullable = false)
    private ItemOrder itemOrder;
    @Column(nullable = false)
    private LocalDate date;
    @Setter
    @Column(nullable = false)
    private Boolean accomplishment;

    @Builder
    public Accomplishment(User user, ItemOrder itemOrder, LocalDate date, Boolean accomplishment) {
        this.user = user;
        this.itemOrder = itemOrder;
        this.date = date;
        this.accomplishment = accomplishment;
    }

    @RequiredArgsConstructor
    public enum AccomplishmentType {
        CLEAR("clear"),
        INCOMPLETE("incomplete"),
        FAIL("fail"),
        IN_PROGRESS("in_progress");

        private final String value;
    }
}

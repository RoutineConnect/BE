package kr.online.routineconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ItemOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @OneToOne(mappedBy = "itemOrder")
    private Accomplishment accomplishment;
    @OneToOne(mappedBy = "itemOrder")
    private Retrospect retrospect;
    @Column(nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;
    @Column(nullable = false)
    private Double position;

    @Builder
    public ItemOrder(User user, Item item, LocalDate date, DayOfWeek day, Double position) {
        this.user = user;
        this.item = item;
        this.date = date;
        this.day = day;
        this.position = position;
    }

    public Boolean userIs(User user) {
        return this.user.equals(user);
    }

    public void updatePositionTo(Double newPosition) {
        this.position = newPosition;
    }
}
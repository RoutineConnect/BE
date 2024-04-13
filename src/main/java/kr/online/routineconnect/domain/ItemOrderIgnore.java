package kr.online.routineconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ItemOrderIgnore {

    @Id
    @Column(updatable = false)
    private Long id;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId
    @OneToOne
    private ItemOrder itemOrder;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    @Builder
    public ItemOrderIgnore(Long id, ItemOrder itemOrder, User user, LocalDate date, DayOfWeek day) {
        this.id = id;
        this.itemOrder = itemOrder;
        this.user = user;
        this.date = date;
        this.day = day;
    }
}

package kr.online.routineconnect.domain;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import kr.online.routineconnect.converter.EnumSetToBitmaskConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Routine.class, name = "routine"),
        // 다른 서브 타입들 추가
})
@DiscriminatorColumn(name = "type")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Setter
@Getter
@Entity
public abstract class Item {

    @ManyToOne
    @JoinColumn(name = "hour_id")
    protected Hour hour;
    @Setter(AccessLevel.NONE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;
    @Column(nullable = false)
    protected String title;
    //    일월화수목금토
//    ________ 0 or 1
    @Convert(converter = EnumSetToBitmaskConverter.class)
    @Column(nullable = false)
    protected EnumSet<DayOfWeek> repeatingDays;
    @Column(nullable = false)
    protected Boolean shared;
    @Column(nullable = false)
    protected LocalDate createdDate;
    @Column
    protected LocalDate endedDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    public Boolean isSetOn(DayOfWeek o) {
        return repeatingDays.contains(o);
    }

    public Boolean userIs(User user) {
        return this.user == user;
    }
}

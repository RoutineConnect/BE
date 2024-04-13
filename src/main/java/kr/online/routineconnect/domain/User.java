package kr.online.routineconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.online.routineconnect.converter.AuthoritiesToStringConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTimeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String password;
    @Convert(converter = AuthoritiesToStringConverter.class)
    private List<GrantedAuthority> authorities;
    @OneToMany(mappedBy = "user")
    private final Set<Hour> hours = new HashSet<>();

    @Builder
    public User(String email, String name, String password, List<GrantedAuthority> authorities) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.authorities = authorities;
    }
}

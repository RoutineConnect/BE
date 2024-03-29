package kr.online.routineconnect.domain;

import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String userEmail;
    private String refreshToken;
    @Value("${jwt.refresh-token.expiration-hour}")
    @TimeToLive(unit = TimeUnit.HOURS)
    private long expirationTime;

    @Builder
    public RefreshToken(String userEmail, String refreshToken) {
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
    }
}

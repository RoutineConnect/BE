package kr.online.routineconnect.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String userEmail;
    private String refreshToken;
    @TimeToLive
    private long expirationTime;

    @Builder
    public RefreshToken(String userEmail, String refreshToken, long expirationTime) {
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }
}

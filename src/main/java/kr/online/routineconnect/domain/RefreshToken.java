package kr.online.routineconnect.domain;

import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String userEmail;
    private String refreshToken;
    @Value("${jwt.refresh-token.expriation-time}")
    @TimeToLive(unit = TimeUnit.HOURS)
    private long expirationTime;

    @Builder
    public RefreshToken(String userEmail, String refreshToken) {
        this.userEmail = userEmail;
        this.refreshToken = refreshToken;
    }
}

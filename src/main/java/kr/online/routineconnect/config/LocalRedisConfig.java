package kr.online.routineconnect.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Profile("!prod")
@Configuration
public class LocalRedisConfig {

    private final RedisServer redisServer;

    public LocalRedisConfig() throws IOException {
        this.redisServer = new RedisServer();
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }
}

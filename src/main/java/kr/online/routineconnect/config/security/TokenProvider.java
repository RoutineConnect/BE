package kr.online.routineconnect.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import kr.online.routineconnect.converter.AuthoritiesToStringConverter;
import kr.online.routineconnect.domain.RefreshToken;
import kr.online.routineconnect.dto.SignInResponse;
import kr.online.routineconnect.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthoritiesToStringConverter authoritiesToStringConverter;
    @Value("${jwt.access-token.secret}")
    private String accessTokenSecret;
    @Value("${jwt.refresh-token.secret}")
    private String refreshTokenSecret;
    @Value("${jwt.access-token.expiration-minute}")
    private long accessTokenExpirationTime;
    @Value("${jwt.refresh-token.expiration-hour}")
    private long refreshTokenExpirationTime;
    private SecretKey accessTokenKey;
    private SecretKey refreshTokenKey;
    private static final String AUTHORITIES = "authorities";

    @PostConstruct
    private void init() {
        var keyBytes = Decoders.BASE64.decode(accessTokenSecret);
        accessTokenKey = Keys.hmacShaKeyFor(keyBytes);
        keyBytes = Decoders.BASE64.decode(refreshTokenSecret);
        refreshTokenKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public SignInResponse createTokens(String email, Collection<? extends GrantedAuthority> authorities) {
        var accessToken = createAccessToken(email, authorities);
        var refreshToken = createRefreshToken(email, authorities);
        refreshTokenRepository.save(RefreshToken.builder()
                .userEmail(email)
                .refreshToken(refreshToken)
                .expirationTime(TimeUnit.HOURS.toSeconds(refreshTokenExpirationTime))
                .build());

        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String createAccessToken(String email, Collection<? extends GrantedAuthority> authorities) {
        var now = Instant.now();

        return Jwts.builder()
                .claims(Map.of(AUTHORITIES, authoritiesToStringConverter.convertToDatabaseColumn(authorities)))
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpirationTime, ChronoUnit.MINUTES)))
                .signWith(accessTokenKey)
                .compact();
    }

    private String createRefreshToken(String email, Collection<? extends GrantedAuthority> authorities) {
        var now = Instant.now();

        return Jwts.builder()
                .claims(Map.of(AUTHORITIES, authoritiesToStringConverter.convertToDatabaseColumn(authorities)))
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenExpirationTime, ChronoUnit.HOURS)))
                .signWith(refreshTokenKey)
                .compact();
    }

    private String createRefreshToken(String email, Collection<? extends GrantedAuthority> authorities,
                                      Date expiration) {
        var now = Instant.now();

        return Jwts.builder()
                .claims(Map.of(AUTHORITIES, authoritiesToStringConverter.convertToDatabaseColumn(authorities)))
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(expiration)
                .signWith(refreshTokenKey)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) throws JwtException, IllegalArgumentException {
        var payload = Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        var authorities = authoritiesToStringConverter.convertToEntityAttribute(payload.get(AUTHORITIES, String.class));
        var principal = new User(payload.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
    }

    public SignInResponse refreshAccessToken(String refreshToken) throws JwtException {
        var token = Jwts.parser()
                .verifyWith(refreshTokenKey)
                .build()
                .parseSignedClaims(refreshToken);

        var payload = token.getPayload();
        var email = payload.getSubject();
        if (!refreshTokenRepository.existsById(email)) {
            throw new ExpiredJwtException(token.getHeader(), payload, "만료된 리프레시 토큰입니다.");
        }

        refreshTokenRepository.deleteById(email);
        var authorities = authoritiesToStringConverter.convertToEntityAttribute(payload.get(AUTHORITIES, String.class));
        var expiration = payload.getExpiration();
        var accessToken = createAccessToken(email, authorities);
        var newRefreshToken = createRefreshToken(email, authorities, expiration);
        refreshTokenRepository.save(RefreshToken.builder()
                .userEmail(email)
                .refreshToken(refreshToken)
                .expirationTime(
                        Duration.between(Instant.now(), expiration.toInstant()).getSeconds())
                .build());

        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}

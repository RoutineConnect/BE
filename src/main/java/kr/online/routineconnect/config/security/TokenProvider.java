package kr.online.routineconnect.config.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import kr.online.routineconnect.domain.RefreshToken;
import kr.online.routineconnect.dto.SignInResponse;
import kr.online.routineconnect.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.access-token.secret}")
    private final String accessTokenSecret;
    @Value("${jwt.refresh-token.secret}")
    private final String refreshTokenSecret;
    @Value("${jwt.access-token.expiration-minute}")
    private final long accessTokenExpirationTime;
    @Value("${jwt.refresh-token.expiration-day}")
    private final long refreshTokenExpirationTime;
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
        var refreshToken = createRefreshToken(email);
        refreshTokenRepository.save(RefreshToken.builder()
                .userEmail(email)
                .refreshToken(refreshToken)
                .build());

        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String createAccessToken(String email, Collection<? extends GrantedAuthority> authorities) {
        var now = Instant.now();

        return Jwts.builder()
                .claims(Map.of(AUTHORITIES,
                        authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","))
                ))
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpirationTime, ChronoUnit.MINUTES)))
                .signWith(accessTokenKey)
                .compact();
    }

    private String createRefreshToken(String email) {
        var now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenExpirationTime, ChronoUnit.HOURS)))
                .signWith(accessTokenKey)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) throws JwtException, IllegalArgumentException {
        var payload = Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        var authorities = Arrays.stream(payload.get(AUTHORITIES, String.class).split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        var principal = new User(payload.getSubject(), null, authorities);

        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
    }
}

package kr.online.routineconnect.config.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
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


                    .claims(Map.of(AUTHORITIES, authoritiesToStringConverter.convertToDatabaseColumn(author
                        .subject(email)

                        .expiration(D
                                .signWith(accessTokenKey)
                                                
                                .compact();
                                
                                
                                ng createRefreshToken(Str
                                = Instant.n
        

                    .claims(Map.of(AUTHORITIES, authoritiesToStringConverter.convertToDatabaseColumn(authorit
                        .subject(email)

                        .expiration(D
                                .signWith(refreshTokenKey)
                                                
                                .compact();
                                
                                
                                ntication getAuthenticatio
                                oad = Jwts.
         

                    .parseSignedClaims(accessToken)
                        .getPayload();
                                
                                orities 
                                cipal = new User(payload.getSub
                                

                
                                
                

                
        
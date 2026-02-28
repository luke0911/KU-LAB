package com.fifthdimension.digital_twin.infrastructure.auth;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j(topic = "JwtProvider")
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secret;

    private SecretKey secretKey;

    private final long ACCESS_TOKEN_EXPIRE = 1000L * 60 * 60; // 1시간

    @PostConstruct
    public void initSecretKey() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(UUID userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(Keys.hmacShaKeyFor(secretKey.getEncoded()), Jwts.SIG.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(UUID userId, String role) {
        // 보통 만료시간은 7~30일로 AccessToken보다 길게
        long expireTimeMs = 1000L * 60 * 60 * 24 * 7; // 7일
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTimeMs))
                .signWith(Keys.hmacShaKeyFor(secretKey.getEncoded()), Jwts.SIG.HS256)
                .compact();
    }

    // JWT 파싱 및 검증
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException e) {
            log.error("SecurityException: " + e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");
        }
    }

    // 만료 허용(AccessToken 재발급 시 필요)
    public Claims parseClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 클레임 반환
            log.warn(e.getMessage());
            return e.getClaims();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

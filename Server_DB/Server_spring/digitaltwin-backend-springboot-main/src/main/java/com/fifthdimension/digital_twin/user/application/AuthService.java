package com.fifthdimension.digital_twin.user.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.infrastructure.auth.JwtProvider;
import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRepository;
import com.fifthdimension.digital_twin.user.dto.UserProfileCachingDto;
import com.fifthdimension.digital_twin.user.dto.auth.LogInReqDto;
import com.fifthdimension.digital_twin.user.dto.auth.LogInResDto;
import com.fifthdimension.digital_twin.user.dto.auth.TokenResDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public LogInResDto logIn(LogInReqDto logInReqDto) {
        User user;
        try{
            user = userRepository.findByAccountIdAndIsDeletedIsFalse(logInReqDto.getAccountId()).orElseThrow();
        }catch (Exception e) {
            log.error("타겟 유저가 존재하지 않습니다.");
            throw new CustomException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호 불일치");
        }

        // 비밀번호 일치 체크
        if(!passwordEncoder.matches(logInReqDto.getPassword(), user.getPassword())){
            log.error("비밀번호 불일치");
            throw new CustomException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호 불일치");
        }

        // Token 생성
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().getValue());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().getValue());

        // Token Redis 저장
        redisTemplate.opsForValue().set("refresh-token:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        // User Profile Caching
        try {
            UserProfileCachingDto profileCachingDto = UserProfileCachingDto.builder()
                    .userName(user.getName())
                    .ageRange(user.getAgeRange())
                    .gender(user.getGender())
                    .build();
            String profileJson = objectMapper.writeValueAsString(profileCachingDto);

            redisTemplate.opsForValue().set("user-profile:" + user.getId(), profileJson, 60, TimeUnit.MINUTES); // TTL 60분
        } catch (JsonProcessingException e) {
            log.error("Profile Caching Data Json Error");
        }

        return new LogInResDto(true, accessToken, refreshToken);
    }

    public void logout(UUID userId, String refreshToken) {
        // 1. RefreshToken이 userId에 속하는지 검증
        try {
            Claims claims = jwtProvider.parseClaims(refreshToken);
            UUID tokenUserId = UUID.fromString(claims.getSubject());

            // userId와 RefreshToken의 소유자가 일치하는지 확인
            if (!userId.equals(tokenUserId)) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "토큰 소유자가 일치하지 않습니다.");
            }

            // 2. Redis에서 저장된 RefreshToken과 동일한지 확인
            String savedRefreshToken = redisTemplate.opsForValue().get("refresh-token:" + userId);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 RefreshToken입니다.");
            }

            // 3. RefreshToken 삭제
            redisTemplate.delete("refresh-token:" + userId);
            // 필요시 AccessToken도 함께 받아서 블랙리스트 등록 고려

        } catch (ExpiredJwtException e) {
            // RefreshToken 만료
            throw new CustomException(HttpStatus.UNAUTHORIZED, "RefreshToken이 만료되었습니다.");
        } catch (JwtException e) {
            // 잘못된 서명, 구조 등
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 RefreshToken입니다.");
        } catch (CustomException e) {
            // 위에서 발생시킨 예외 그대로 재던짐
            throw e;
        } catch (Exception e) {
            // 기타 서버 내부 에러
            log.error("로그아웃 처리 중 서버 오류: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");
        }
    }

    public TokenResDto refreshAccessToken(String expiredAccessToken, String refreshToken) {
        // 1. 만료된 AccessToken에서 Claims 추출 (만료 허용 파서 사용)
        Claims accessClaims;
        try {
            accessClaims = jwtProvider.parseClaimsAllowExpired(expiredAccessToken);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "만료된 AccessToken이 유효하지 않습니다.");
        }
        UUID accessUserId = UUID.fromString(accessClaims.getSubject());
        String accessRole = accessClaims.get("role", String.class);

        // 2. RefreshToken에서 Claims 추출
        Claims refreshClaims;
        try {
            refreshClaims = jwtProvider.parseClaims(refreshToken);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "RefreshToken이 유효하지 않습니다.");
        }
        UUID refreshUserId = UUID.fromString(refreshClaims.getSubject());
        String refreshRole = refreshClaims.get("role", String.class);

        // 3. AccessToken/RefreshToken의 userId 및 role 일치하는지 확인
        if (!accessUserId.equals(refreshUserId) || !accessRole.equals(refreshRole)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "토큰의 소유자가 일치하지 않습니다.");
        }


        // 4. Redis에서 실제 저장된 RefreshToken과 비교
        String savedRefreshToken = redisTemplate.opsForValue().get("refresh-token:" + refreshUserId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "RefreshToken이 유효하지 않습니다.");
        }

        // 5. RefreshToken 만료 임박 체크 (예: 3일 이하 남으면 갱신)
        Date refreshExpireTime = refreshClaims.getExpiration();
        long now = System.currentTimeMillis();
        long remainMs = refreshExpireTime.getTime() - now;

        final long THREE_DAYS_MS = 1000L * 60 * 60 * 24 * 3;
        String newRefreshToken = refreshToken;
        if (remainMs < THREE_DAYS_MS) {
            // 만료 임박, 새 토큰 발급 및 Redis 업데이트
            newRefreshToken = jwtProvider.createRefreshToken(refreshUserId, refreshRole);
            redisTemplate.opsForValue().set("refresh-token:" + refreshUserId, newRefreshToken, 7, TimeUnit.DAYS);
        }

        // 6. 새로운 AccessToken 발급
        String newAccessToken = jwtProvider.createAccessToken(refreshUserId, refreshRole);

        // 응답에 새 RefreshToken도 포함해서 반환 (만료 임박이 아니면 기존 값 그대로)
        return new TokenResDto(newAccessToken, newRefreshToken);
    }
}

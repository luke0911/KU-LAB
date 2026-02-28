package com.fifthdimension.digital_twin.infrastructure.auditing;

import com.fifthdimension.digital_twin.infrastructure.auth.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private final JwtProvider jwtProvider;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return Optional.empty();

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtProvider.parseClaims(token);
                UUID userId = UUID.fromString(claims.getSubject());
                return Optional.of(userId);
            } catch (Exception e) {
                // 토큰 만료, 포맷 오류 등
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}

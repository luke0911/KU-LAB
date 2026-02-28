package com.fifthdimension.digital_twin.infrastructure.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifthdimension.digital_twin.global.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j(topic = "JwtAuthFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final String[] originWhiteList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String[] WHITE_LIST = {
            "/api/auth/login", "/api/users/idCheck", "/api/swagger-ui/**", "/api/v3/docs/**", "/api/auth/refresh",
            "/api/push/**"
    };
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. CORS 허용 Origin 처리
        if (!setCorsHeaderIfWhiteListed(request, response)) {
            // 허용되지 않은 Origin이면 바로 차단 (에러 응답 필요하면 아래 주석 해제)
            // writeErrorResponse(response, 403, "CORS 정책에 의해 차단되었습니다.");
            return;
        }

        // 2. JWT 인증 화이트리스트 URI 체크
        String uri = request.getRequestURI();
        String basePath = request.getContextPath();

        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(basePath + pattern, uri)) {
                filterChain.doFilter(request, response); // 화이트리스트는 인증 무시
                return;
            }
        }

        if (pathMatcher.match(basePath + "/api/users", uri) && request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response); // 회원 가입 무시
            return;
        }

        // 3. JWT 검사
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Authorization header missing or invalid.");
            writeErrorResponse(request, response, 401, "Authorization 헤더가 필요합니다. (예: Bearer {token})");
            return;
        }

        String token = header.substring(7);

        try {
            var claims = jwtProvider.parseClaims(token);
            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(userId, role, authorities),
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            log.error("JWT Expired: {}", e.getMessage());
            writeErrorResponse(request, response, 401, "AccessToken이 만료되었습니다. RefreshToken으로 토큰을 재발급 받으세요.");
            return;
        } catch (JwtException e) {
            log.error("JWT Invalid: {}", e.getMessage());
            writeErrorResponse(request, response, 401, "유효하지 않은 AccessToken입니다.");
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("JWT 처리 중 예외: {}", e.getMessage());
            writeErrorResponse(request, response, 500, "서버 내부 오류입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ----- CORS 관련 유틸 -----

    private boolean setCorsHeaderIfWhiteListed(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin == null) return true; // non-browser client or same-origin

        for (String allowed : originWhiteList) {
            if (allowed != null && allowed.equals(origin)) {
                response.setHeader("Access-Control-Allow-Origin", allowed);
                response.setHeader("Vary", "Origin");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
                return true;
            }
        }
        return false;
    }

    // ----- 에러 응답 공통화 -----

    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        // 반드시 CORS 헤더도 같이 세팅!
        setCorsHeaderIfWhiteListed(request, response);
        ErrorResponse errorResponse = new ErrorResponse(status, message);
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
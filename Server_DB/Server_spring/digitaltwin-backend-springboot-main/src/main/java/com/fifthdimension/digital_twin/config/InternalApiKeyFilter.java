package com.fifthdimension.digital_twin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifthdimension.digital_twin.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j(topic = "InternalAPIKeyFilter")
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String internalApiKey;

    // 내부 API URL 패턴들만 필터링
    private final List<String> internalApiPatterns = List.of(
            "/api/push/**" // 내부 호출만 허용할 엔드포인트 패턴들
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 내부 API에만 체크

        String basePath = request.getContextPath();
        boolean requiresInternalKey = internalApiPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(basePath + pattern, request.getRequestURI()));

        if (requiresInternalKey) {
            String reqKey = request.getHeader("X-Internal-Api-Key");
            if (reqKey == null || !reqKey.equals(internalApiKey)) {
                String message = "Invalid or missing Internal API Key! path="+ request.getRequestURI()
                        + ", ip=" + request.getRemoteAddr();
                log.warn(message);
                int statusCode = HttpStatus.FORBIDDEN.value();
                ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
                response.setStatus(statusCode);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
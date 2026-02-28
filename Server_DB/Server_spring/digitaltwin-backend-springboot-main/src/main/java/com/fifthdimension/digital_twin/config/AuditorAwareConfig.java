package com.fifthdimension.digital_twin.config;

import com.fifthdimension.digital_twin.infrastructure.auditing.AuditorAwareImpl;
import com.fifthdimension.digital_twin.infrastructure.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class AuditorAwareConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new AuditorAwareImpl(jwtProvider);
    }
}

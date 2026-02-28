package com.fifthdimension.digital_twin.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfigurer implements WebMvcConfigurer {

    @Value("${server.host}")
    private String serverHost;

    private String[] originWhiteList;

    @PostConstruct
    public void initializeOriginWhiteList() {
        originWhiteList = new String[]{
                serverHost,

                // 개발용
                "http://localhost:8080",
                "https://localhost:8080",
                "http://localhost:5000",
                "https://localhost:5000",
                "http://localhost:3000",
                "https://localhost:3000",
        };
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(originWhiteList)
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(3600);

        WebMvcConfigurer.super.addCorsMappings(registry);
    }
}

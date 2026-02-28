package com.fifthdimension.digital_twin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@OpenAPIDefinition(
        info = @Info(
                title = "Fifth Dimension Digital Twin API",
                description = "Digital Twin Spring Boot 프로젝트 API 명세"
        )
)
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path}")
    private String base_path;

    @Value("${server.host}")
    private String serverHost;

    private Server[] servers;

    @PostConstruct
    public void initializeServers() {
        servers = new Server[]{
                new Server().url(serverHost + base_path).description("Prod"),
                new Server().url("http://localhost:8080" + base_path).description("Dev")
        };
    }

    // 추후 JWT 토큰 사용할 부분 고려해서 적용
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.stream(servers).toList())
                .components(new Components()
                        .addSecuritySchemes("JWT-Token", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("JWT-Token"));
    }

}

package com.leejeonghoon.blogproject.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "BearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Value("${server.port}")
    private String port;

    @Value("${server.servlet.context-path:}") // 기본값을 빈 문자열로 설정
    private String contextPath;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("BearerAuth")))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Blog-Project API")
                .description("혼자서 해보는 프로젝트 API 명세서 입니다.")
                .version("1.0.0");
    }
}

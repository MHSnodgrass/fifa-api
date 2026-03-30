package com.snodgrass.fifa_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIFA World Cup 2026 API")
                        .version("1.0.0")
                        .description("Read-only API for FIFA World Cup 2026 teams and match (mock) data"));
    }
}

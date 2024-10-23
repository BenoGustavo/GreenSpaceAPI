package com.greenspace.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfiguration {

        private static final String[] END_POINTS = {};

        @Bean
        public GroupedOpenApi publicApi() {
                return GroupedOpenApi.builder()
                                .pathsToMatch(END_POINTS)
                                .group("public-api")
                                .build();
        }

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Green Space API Documentação Swagger")
                                                .version("1.0")
                                                .description("Uma API desenvolvida para alimentar a rede social GreenSpace.")
                                                .termsOfService("http://swagger.io/terms/")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer").bearerFormat("JWT")));
        }
}
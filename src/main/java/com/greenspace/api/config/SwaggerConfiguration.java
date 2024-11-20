package com.greenspace.api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
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

        @Value("${spring.application.name}")
        private String APPLICATION_NAME;

        @Value("${spring.application.description}")
        private String APPLICATION_DESCRIPTION;

        private static final String[] END_POINTS = {
                        "/api/**", "/admin/api/**"
        };

        @Bean
        public GroupedOpenApi publicApi() {
                return GroupedOpenApi.builder()
                                .pathsToMatch(END_POINTS)
                                .group(APPLICATION_NAME)
                                .build();
        }

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title(APPLICATION_NAME + " Documentação Swagger")
                                                .version("1.0")
                                                .description(APPLICATION_DESCRIPTION)
                                                .termsOfService("http://swagger.io/terms/")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer").bearerFormat("JWT")));
        }
}
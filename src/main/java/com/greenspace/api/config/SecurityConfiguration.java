package com.greenspace.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.greenspace.api.error.filter.ExceptionHandlerFilter;
import com.greenspace.api.features.user.UserValidationFilter;
import com.greenspace.api.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

        @Autowired
        AuthenticationProvider authenticationProvider;

        @Autowired
        JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired
        ExceptionHandlerFilter exceptionHandlerFilter;

        @Autowired
        UserValidationFilter userValidationFilter;

        private final String[] WHITE_LIST = {
                        "/api/auth/**", "/", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/swagger-ui/**", "/public/api/v1/auth/**"
        };

        private final String[] AUTHENTICATED_ROUTES = {
                        "/public/api/v1/auth/logout"
        };

        private final static String[] ONLY_ADMIN_ALLOWED_ROUTES = {
                        "admin/api/v1/**"
        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                                .requestMatchers(AUTHENTICATED_ROUTES).authenticated()
                                                .requestMatchers(WHITE_LIST).permitAll()
                                                .requestMatchers(ONLY_ADMIN_ALLOWED_ROUTES).hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .securityMatcher(WHITE_LIST)

                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(sessionManagement -> sessionManagement
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(exceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(userValidationFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public WebMvcConfigurer corsConfigurer() {
                return new WebMvcConfigurer() {
                        @Override
                        public void addCorsMappings(@NonNull CorsRegistry registry) {
                                registry.addMapping("/**")
                                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                                .allowedOrigins("*")
                                                .allowedHeaders("*");
                        }
                };
        }
}

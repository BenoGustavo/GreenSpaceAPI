package com.greenspace.api.features.user;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.greenspace.api.error.http.Unauthorized401Exception;
import com.greenspace.api.models.UserModel;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserModel user = (UserModel) authentication.getPrincipal();

            if (user.getDeletedAt() != null) {
                throw new Unauthorized401Exception("User is currently deleted user");
            }

            if (user.getIsBanned()) {
                throw new Unauthorized401Exception("User is banned");
            }

            if (!user.getIsEmailValidated()) {
                throw new Unauthorized401Exception("Account not verified, check your email address or signup");
            }
        }

        filterChain.doFilter(request, response);
    }
}
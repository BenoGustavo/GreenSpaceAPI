package com.greenspace.api.features.user;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.error.http.Unauthorized401Exception;
import com.greenspace.api.features.user.banned.BannedUsersRepository;
import com.greenspace.api.models.BannedUsersModel;
import com.greenspace.api.models.UserModel;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserValidationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            UserModel user = userRepository.findByEmailAddress(authentication.getName()).orElseThrow(
                    () -> new NotFound404Exception("User not found, please login again"));

            Optional<BannedUsersModel> bannedUser = bannedUsersRepository
                    .findByUserEmailAddress(user.getEmailAddress());

            if (user.getDeletedAt() != null) {
                throw new Unauthorized401Exception("User is currently deleted user");
            }

            if (user.getBan() != null) {
                throw new Unauthorized401Exception("User is banned");
            }

            if (!user.getIsEmailValidated()) {
                throw new Unauthorized401Exception("Account not verified, check your email address or signup");
            }

            if (bannedUser.isPresent()) {
                if (user.getBan() != null) {
                    throw new Unauthorized401Exception("User is banned");
                }
            }

            if (user.getIsDeactivated()) {
                throw new Unauthorized401Exception(
                        "Account is currently deactivated, the owner must reactivate it to continue");
            }
        }

        filterChain.doFilter(request, response);
    }
}
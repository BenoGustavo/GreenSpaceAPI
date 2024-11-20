package com.greenspace.api.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.features.user.permissions.PermissionsRepository;
import com.greenspace.api.models.UserModel;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class AdminUser {
    private final UserRepository userRepository;
    private final PermissionsRepository permissionsRepository;
    private final Dotenv dotenv;
    private final PasswordEncoder passwordEncoder;

    public AdminUser(UserRepository userRepository, PermissionsRepository permissionsRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.permissionsRepository = permissionsRepository;
        this.dotenv = Dotenv.load();
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    @Order(2)
    public CommandLineRunner seedAdminUser() {
        return args -> {
            String adminEmail = dotenv.get("ADMIN_EMAIL");
            String adminUsername = dotenv.get("ADMIN_USERNAME");
            String adminName = dotenv.get("ADMIN_NAME");
            String adminPassword = dotenv.get("ADMIN_PASSWORD");

            if (adminEmail == null || adminEmail.isEmpty()) {
                System.out.println("\n\nADMIN USER CREATION FAILED! ADMIN_EMAIL is missing or empty!\n\n");
                return;
            }
            if (adminUsername == null || adminUsername.isEmpty()) {
                System.out.println("\n\nADMIN USER CREATION FAILED! ADMIN_USERNAME is missing or empty!\n\n");
                return;
            }
            if (adminName == null || adminName.isEmpty()) {
                System.out.println("\n\nADMIN USER CREATION FAILED! ADMIN_NAME is missing or empty!\n\n");
                return;
            }
            if (adminPassword == null || adminPassword.isEmpty()) {
                System.out.println("\n\nADMIN USER CREATION FAILED! ADMIN_PASSWORD is missing or empty!\n\n");
                return;
            }

            if (userRepository.findByEmailAddress(adminEmail).isPresent()) {
                System.out.println("\n\nADMIN USER ALREADY EXISTS! EMAIL ADDRESS ALREADY IN USE!\n\n");
                return;
            }
            if (userRepository.findByUsername(adminUsername).isPresent()) {
                System.out.println("\n\nADMIN USER ALREADY EXISTS! USERNAME ALREADY IN USE!\n\n");
                return;
            }

            UserModel adminUser = UserModel.builder()
                    .emailAddress(adminEmail)
                    .username(adminUsername)
                    .nickname(adminName)
                    .password(passwordEncoder.encode(adminPassword))
                    .isEmailValidated(true)
                    .isDeactivated(false)
                    .isOnline(false)
                    .permissionLevel(permissionsRepository.findByName(PermissionLevel.ROLE_ADMIN).orElse(null))
                    .build();

            if (adminUser.getPermissionLevel() == null) {
                System.out.println("\n\nADMIN USER CREATION FAILED! ADMIN PERMISSION LEVEL NOT FOUND!\n\n");
                return;
            }

            System.out.println(
                    "\n\nSEED ADMIN LOG MESSAGE:\n" + userRepository.save(adminUser) + "\n ADMIN USER CREATED!");
        };
    }
}

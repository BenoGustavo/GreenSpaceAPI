package com.greenspace.api.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.features.user.permissions.PermissionsRepository;
import com.greenspace.api.models.PermissionModel;

@Configuration
public class BasicPermissionsSeed {

    private final PermissionsRepository permissionsRepository;

    public BasicPermissionsSeed(PermissionsRepository permissionsRepository) {
        this.permissionsRepository = permissionsRepository;
    }

    @Bean
    @Order(2)
    public CommandLineRunner seedPermissions() {
        return args -> {
            if (!permissionsRepository.findByName(PermissionLevel.ROLE_ADMIN).isPresent()) {
                PermissionModel adminRole = new PermissionModel();
                adminRole.setName(PermissionLevel.ROLE_ADMIN);
                adminRole.setDescription("A role that grants full access to the system.");

                permissionsRepository.save(adminRole);
            }

            if (!permissionsRepository.findByName(PermissionLevel.ROLE_USER).isPresent()) {
                PermissionModel userRole = new PermissionModel();
                userRole.setName(PermissionLevel.ROLE_USER);
                userRole.setDescription("A role that grants basic access to the system.");

                permissionsRepository.save(userRole);
            }
        };
    }
}

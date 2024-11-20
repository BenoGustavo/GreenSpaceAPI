package com.greenspace.api.features.user.permissions;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenspace.api.enums.PermissionLevel;
import com.greenspace.api.models.PermissionModel;

public interface PermissionsRepository extends JpaRepository<PermissionModel, UUID> {
    Optional<PermissionModel> findByName(PermissionLevel name);

}

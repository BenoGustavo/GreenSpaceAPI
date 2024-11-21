package com.greenspace.api.features.user.banned;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenspace.api.models.BanRevokeLogModel;

public interface BanRevokeRepository extends JpaRepository<BanRevokeLogModel, UUID> {
}

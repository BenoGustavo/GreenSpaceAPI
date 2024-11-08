package com.greenspace.api.features.address;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenspace.api.models.AddressModel;

import jakarta.transaction.Transactional;

public interface AddressRepository extends JpaRepository<AddressModel, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE AddressModel a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.id = :id")
    void softdelete(@Param("id") UUID id);
}

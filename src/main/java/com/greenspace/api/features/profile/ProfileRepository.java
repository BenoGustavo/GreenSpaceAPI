package com.greenspace.api.features.profile;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.greenspace.api.models.ProfileModel;

public interface ProfileRepository extends JpaRepository<ProfileModel, UUID> {

}

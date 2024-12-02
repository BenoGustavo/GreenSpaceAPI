package com.greenspace.api.features.imagesManager;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.greenspace.api.dto.UserImagesDTO;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.models.UserImagesModel;

public interface UserImagesRepository extends JpaRepository<UserImagesModel, UUID> {
        List<UserImagesModel> findByUserId(UUID userId);

        @Query("SELECT COUNT(u) FROM UserImagesModel u WHERE u.user.id = :userId")
        long countUserImageQuantity(@Param("userId") UUID userId);

        @Modifying
        @Transactional
        @Query("DELETE FROM UserImagesModel u WHERE u.user.id = :userId AND u.imageType = :imageType")
        void deleteUserImageByType(@Param("userId") UUID userId, @Param("imageType") ImageType imageType);

        List<UserImagesModel> findByUserIdAndImageType(UUID userId, ImageType imageType);

        @Query("SELECT new com.greenspace.api.dto.UserImagesDTO(u.id, u.imageUrl, u.imageType) " +
                        "FROM UserImagesModel u WHERE u.user.id = :userId AND u.imageType = :imageType")
        List<UserImagesDTO> findUserImagesWithoutUserInfo(
                        @Param("userId") UUID userId,
                        @Param("imageType") ImageType imageType);

        Boolean existsByImageName(String imageName);

        UserImagesModel findByImageName(String imageName);

        @Modifying
        @Transactional
        void deleteByImageName(String imageName);

        List<UserImagesModel> findByImageType(ImageType imageType);
}

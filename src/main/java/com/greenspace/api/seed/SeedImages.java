package com.greenspace.api.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.greenspace.api.enums.ImageType;
import com.greenspace.api.features.imagesManager.UserImagesRepository;
import com.greenspace.api.models.UserImagesModel;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class SeedImages {
    private final UserImagesRepository userImagesRepository;
    private final Dotenv dotenv;

    public SeedImages(
            UserImagesRepository userImagesRepository) {
        this.userImagesRepository = userImagesRepository;
        this.dotenv = Dotenv.load();
    }

    @Bean
    @Order(1)
    public CommandLineRunner seedDefaultProfilePicture() {
        return args -> {
            if (userImagesRepository.findByImageType(ImageType.DEFAULT_PICTURE).isEmpty()) {
                String defaultProfilePictureUrl = dotenv.get("DEFAULT_PROFILE_PICTURE_URL");

                if (defaultProfilePictureUrl == null) {
                    throw new RuntimeException("DEFAULT_PROFILE_PICTURE_URL not found in .env file");
                }

                UserImagesModel defaultProfilePicture = UserImagesModel.builder()
                        .imageName("default_profile_picture")
                        .imageUrl(defaultProfilePictureUrl)
                        .imageType(ImageType.DEFAULT_PICTURE)
                        .build();

                userImagesRepository.save(defaultProfilePicture);

                System.out.println("\n\nDEFAULT PROFILE PICTURE SEEDED\n\n");
            }
        };
    }

}

package com.greenspace.api.features.profile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.greenspace.api.dto.profile.ProfileDTO;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.error.http.NotFound404Exception;
import com.greenspace.api.features.imagesManager.UserImagesRepository;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.ProfileModel;
import com.greenspace.api.models.UserImagesModel;
import com.greenspace.api.models.UserModel;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final Jwt jwtManager;
    private final UserRepository userRepository;
    private final UserImagesRepository userImagesRepository;

    public ProfileService(
            ProfileRepository profileRepository,
            Jwt jwtManager,
            UserRepository userRepository,
            UserImagesRepository userImagesRepository) {
        this.profileRepository = profileRepository;
        this.jwtManager = jwtManager;
        this.userRepository = userRepository;
        this.userImagesRepository = userImagesRepository;
    }

    public ProfileModel create(UserModel owner) {
        // Pega a imagem de perfil padrão, a que tem o tipo DEFAULT_PICTURE deve ser
        // unica, portanto pega a primeira
        UserImagesModel defaultProfilePicture = userImagesRepository.findByImageType(ImageType.DEFAULT_PICTURE).get(0);

        ProfileModel profile = ProfileModel.builder()
                .bio("")
                .description("")
                .privateAccount(false)
                .profilePicture(defaultProfilePicture.getImageUrl())
                .build();

        return profileRepository.save(profile);
    }

    public ProfileModel update(UUID profileId, ProfileDTO newProfile) {
        // Pega o perfil que ja existe do banco de dados
        ProfileModel existingProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFound404Exception("Profile not found with id " + profileId));

        // Comparar os campos do perfil existente com os campos do novo perfil para ver
        // o que precisa atualizar
        if (newProfile.getBio() != null && !newProfile.getBio().equals(existingProfile.getBio())) {
            existingProfile.setBio(newProfile.getBio());
        }
        if (newProfile.getDescription() != null
                && !newProfile.getDescription().equals(existingProfile.getDescription())) {
            existingProfile.setDescription(newProfile.getDescription());
        }
        if (newProfile.getPrivateAccount() != null
                && !newProfile.getPrivateAccount().equals(existingProfile.getPrivateAccount())) {
            existingProfile.setPrivateAccount(newProfile.getPrivateAccount());
        }
        if (newProfile.getProfilePicture() != null
                && !newProfile.getProfilePicture().equals(existingProfile.getProfilePicture())) {
            existingProfile.setProfilePicture(newProfile.getProfilePicture());
        }

        // Save the updated profile back to the database
        return profileRepository.save(existingProfile);
    }

    // ISSO AQUI DA SOFTDELETE NO PROFILE
    public ProfileModel softdeleteUserProfile() {
        // Pega o usuario logado
        String userEmail = jwtManager.getCurrentUserEmail();
        UserModel loggedUser = userRepository.findByEmailAddress(userEmail)
                .orElseThrow(() -> new NotFound404Exception("User not found with email " + userEmail));

        ProfileModel profile = profileRepository.findById(loggedUser.getProfile().getId())
                .orElseThrow(() -> new NotFound404Exception("Profile from the user " + userEmail + " wasn't found"));

        // Muda a data de deletado para a data atual
        profile.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));

        return profileRepository.save(profile);
    }

    // ESSE METODO DESFAZ O SOFTDELETE NO PROFILE
    public ProfileModel restoreProfile(UserModel user) {
        ProfileModel profile = user.getProfile();

        // Muda a data de deletado para null
        profile.setDeletedAt(null);

        return profileRepository.save(profile);
    }
}

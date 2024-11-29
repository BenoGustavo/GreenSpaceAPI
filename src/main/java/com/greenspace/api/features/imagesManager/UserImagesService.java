package com.greenspace.api.features.imagesManager;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.utils.ObjectUtils;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.features.user.UserRepository;
import com.greenspace.api.jwt.Jwt;
import com.greenspace.api.models.UserImagesModel;
import com.greenspace.api.models.UserModel;

@Service
public class UserImagesService {
    private final ImageUploader imageUploader;
    private final Jwt jwtManager;
    private final UserRepository userRepository;
    private final UserImagesRepository userImagesRepository;

    public UserImagesService(
            ImageUploader imageUploader,
            Jwt jwtManager,
            UserRepository userRepository,
            UserImagesRepository userImagesRepository) {
        this.imageUploader = imageUploader;
        this.jwtManager = jwtManager;
        this.userRepository = userRepository;
        this.userImagesRepository = userImagesRepository;
    }

    @SuppressWarnings("unchecked") // Unckecked por conta do cloudinary que retorna um map de string e object
    public UserImagesModel uploadProfilePicture(MultipartFile file, UserModel user, ImageType imageType) {

        var options = ObjectUtils.asMap(
                "public_id", "user_" + user.getId() + "_" + imageType.toString().toLowerCase(),
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true);

        String profilePictureName = "profile_picture_" + user.getId();

        // Deleta a imagem que já existe do usuario
        userImagesRepository.deleteUserImageByType(user.getId(), ImageType.PROFILE_PICTURE);

        // Sobe a foto para o cloudinary
        String imageUrl = imageUploader.uploadImage(file, user, imageType, profilePictureName, options);

        // Indexa a imagem no banco
        UserImagesModel userImage = UserImagesModel.builder()
                .user(user)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .imageName(options.get("public_id").toString())
                .build();

        UserImagesModel newImage = userImagesRepository.save(userImage);

        return newImage;
    }

    // This one should be implemented in the future
    @SuppressWarnings("unchecked") // Unckecked por conta do cloudinary que retorna um map de string e object
    public UserImagesModel uploadPicture(MultipartFile file, String pictureName, ImageType imageType) {
        UserModel loggedUser = userRepository.findByEmailAddress(
                jwtManager.getCurrentUserEmail()).orElseThrow(
                        () -> new BadRequest400Exception(
                                "Logged user not found on database, how do you even got here?"));

        var options = ObjectUtils.asMap(
                "public_id",
                "article_"
                        + loggedUser.getId()
                        + "_"
                        + imageType.toString().toLowerCase()
                        + "_"
                        + userImagesRepository.countUserImageQuantity(loggedUser.getId()),
                "use_filename", true,
                "unique_filename", false,
                "overwrite", false);

        // Sobe a foto para o cloudinary
        String imageUrl = imageUploader.uploadImage(file, loggedUser, imageType, pictureName, options);

        // Indexa a imagem no banco
        UserImagesModel userImage = UserImagesModel.builder()
                .user(loggedUser)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .imageName(options.get("public_id").toString())
                .build();

        UserImagesModel newImage = userImagesRepository.save(userImage);

        return newImage;
    }
}

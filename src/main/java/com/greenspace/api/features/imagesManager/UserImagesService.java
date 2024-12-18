package com.greenspace.api.features.imagesManager;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.utils.ObjectUtils;
import com.greenspace.api.dto.UserImagesDTO;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.error.http.NotFound404Exception;
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
                                "overwrite", true,
                                "quality", "auto:eco");

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
                                "overwrite", false,
                                "quality", "auto:good");

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

        public List<UserImagesDTO> getAllUserImages() {
                UserModel loggedUser = userRepository.findByEmailAddress(
                                jwtManager.getCurrentUserEmail()).orElseThrow(
                                                () -> new BadRequest400Exception(
                                                                "Logged user not found on database, how do you even got here?"));

                return userImagesRepository.findUserImagesWithoutUserInfo(loggedUser.getId(),
                                ImageType.ARTICLE_PICTURE);
        }

        public UserImagesModel getUserImageById(UUID id) {
                return userImagesRepository.findById(id).orElseThrow(
                                () -> new NotFound404Exception("Image not found on database"));
        }

        public void deleteUserImageById(UUID id) {
                UserModel loggedUser = userRepository.findByEmailAddress(
                                jwtManager.getCurrentUserEmail()).orElseThrow(
                                                () -> new BadRequest400Exception(
                                                                "Logged user not found on database, how do you even got here?"));

                UserImagesModel imageToDelete = userImagesRepository.findById(id).orElseThrow(
                                () -> new NotFound404Exception("Image not found on database"));

                // Valida se o usuario é o dono da imagem!
                if (!imageToDelete.getUser().getId().equals(loggedUser.getId())) {
                        throw new BadRequest400Exception("You can't delete an image that is not yours");
                }

                userImagesRepository.deleteById(id);
                imageUploader.removeImage(imageToDelete.getImageName());
        }

        @SuppressWarnings("unchecked") // Unckecked por conta do cloudinary que retorna um map de string e object
        public String registerDefaultProfilePicture(MultipartFile file) {
                UserModel loggedUser = userRepository.findByEmailAddress(
                                jwtManager.getCurrentUserEmail()).orElseThrow(
                                                () -> new BadRequest400Exception(
                                                                "Logged user not found on database, how do you even got here?"));

                var options = ObjectUtils.asMap(
                                "public_id", "default_profile_picture",
                                "use_filename", true,
                                "unique_filename", false,
                                "overwrite", true,
                                "quality", "auto:eco");

                // faz upload da nova foto padrao
                String profilePictureUrl = imageUploader.uploadImage(
                                file, loggedUser,
                                ImageType.DEFAULT_PICTURE,
                                "default_profile_picture", options);

                // cria a imagem padrao no banco
                UserImagesModel defaultUserImage = UserImagesModel.builder()
                                .user(loggedUser)
                                .imageUrl(profilePictureUrl)
                                .imageType(ImageType.DEFAULT_PICTURE)
                                .imageName(options.get("public_id").toString())
                                .build();

                // Verifica se ja existe uma imagem padrao no banco e deleta caso exista
                if (userImagesRepository.existsByImageName("default_profile_picture")) {
                        userImagesRepository.deleteByImageName("default_profile_picture");
                }

                userImagesRepository.save(defaultUserImage);

                return profilePictureUrl;
        }
}

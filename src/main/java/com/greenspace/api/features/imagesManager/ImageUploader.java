package com.greenspace.api.features.imagesManager;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.error.http.BadRequest400Exception;
import com.greenspace.api.models.UserModel;

import io.github.cdimascio.dotenv.Dotenv;

@Component
public class ImageUploader {

    private final Cloudinary cloudinary;

    public ImageUploader() {

        Dotenv dotenv = Dotenv.load();
        this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    public String uploadImage(
            MultipartFile file,
            UserModel user,
            ImageType imageType,
            String imageName,
            Map<String, Object> options) {

        try {
            // Sobe a foto para o cloudinary
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            // pega o url da imagem e salva no banco
            String imageUrl = uploadResult.get("url").toString();

            return imageUrl;
        } catch (IOException e) {
            throw new BadRequest400Exception("Failed to upload image: " + e.getMessage());
        }
    }

    public void removeImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new BadRequest400Exception("Failed to remove image: " +
                    e.getMessage());
        }
    }
}
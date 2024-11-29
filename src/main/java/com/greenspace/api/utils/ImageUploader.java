package com.greenspace.api.utils;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.error.http.BadRequest400Exception;

import io.github.cdimascio.dotenv.Dotenv;

@Component
public class ImageUploader {

    private final Cloudinary cloudinary;

    public ImageUploader() {
        Dotenv dotenv = Dotenv.load();
        this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    public String uploadImage(MultipartFile file, UUID userId, ImageType imageType) {
        try {
            var options = ObjectUtils.asMap(
                    "public_id", "user_" + userId + "_" + imageType.toString().toLowerCase(),
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true);

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new BadRequest400Exception("Failed to upload image: " + e.getMessage());
        }
    }

    // public void removeImage(String publicId) {
    // try {
    // cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    // } catch (IOException e) {
    // throw new BadRequest400Exception("Failed to remove image: " +
    // e.getMessage());
    // }
    // }
}
package com.greenspace.api.utils;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.greenspace.api.error.http.BadRequest400Exception;

import io.github.cdimascio.dotenv.Dotenv;

@Component
public class ImageUploader {

    private final Cloudinary cloudinary;

    public ImageUploader() {
        Dotenv dotenv = Dotenv.load();
        this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    public String uploadImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.emptyMap());
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new BadRequest400Exception("Failed to upload image: " + e.getMessage());
        }
    }
}
package com.greenspace.api.features.imagesManager;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenspace.api.dto.UserImagesDTO;
import com.greenspace.api.dto.responses.Response;
import com.greenspace.api.enums.ImageType;
import com.greenspace.api.models.UserImagesModel;

@RestController
@RequestMapping("/api/images")
public class UserImagesController {
    private final UserImagesService userImagesService;

    public UserImagesController(UserImagesService userImagesService) {
        this.userImagesService = userImagesService;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Response<Object>> uploadNewPicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pictureName") String pictureName) {

        UserImagesModel newImage = userImagesService.uploadPicture(file, pictureName, ImageType.ARTICLE_PICTURE);

        Response<Object> response = Response.builder()
                .message("Image uploaded successfully")
                .status(201)
                .data(newImage)
                .build();

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Response<Object>> getAllLoggedUserPictures() {
        List<UserImagesDTO> userImages = userImagesService.getAllUserImages();

        Response<Object> response = Response.builder()
                .message("Images retrieved successfully")
                .status(200)
                .data(userImages)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Object>> getPictureById(@PathVariable("id") UUID id) {
        UserImagesModel userImage = userImagesService.getUserImageById(id);

        Response<Object> response = Response.builder()
                .message("Image retrieved successfully")
                .status(200)
                .data(userImage)
                .build();

        return ResponseEntity.ok(response);
    }
}
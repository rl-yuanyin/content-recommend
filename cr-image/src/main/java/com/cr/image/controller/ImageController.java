package com.cr.image.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.dto.ImagePredictionDTO;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.image.dto.ImageDTO;
import com.cr.image.dto.ImageQueryDTO;
import com.cr.image.entity.Image;
import com.cr.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * Image API.
 */
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Uploads an image and creates metadata.
     *
     * @param userId uploader identifier
     * @param dto image metadata and multipart file
     * @return stored image
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Result<Image> uploadImage(
            @RequestHeader(value = "userId", required = false) Long userId,
            @Valid @ModelAttribute ImageDTO dto) {
        if (userId == null) {
            return unauthorized();
        }
        return imageService.uploadImage(userId, dto.getFile(), dto);
    }

    /**
     * Updates image metadata.
     *
     * @param imageId image identifier
     * @param dto image metadata
     * @return update result
     */
    @PutMapping("/update")
    public Result<Void> updateImage(
            @RequestParam Long imageId,
            @Valid @RequestBody ImageDTO dto) {
        return imageService.updateImage(imageId, dto);
    }

    /**
     * Deletes an image.
     *
     * @param id image identifier
     * @return deletion result
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteImage(@PathVariable Long id) {
        return imageService.deleteImage(id);
    }

    /**
     * Gets image details.
     *
     * @param id image identifier
     * @return image details
     */
    @GetMapping("/{id}")
    public Result<Image> getImageById(@PathVariable Long id) {
        return imageService.getImageById(id);
    }

    /**
     * Gets a paginated image list.
     *
     * @param dto query parameters
     * @return image page
     */
    @GetMapping("/list")
    public Result<Page<Image>> getImageList(@Valid ImageQueryDTO dto) {
        return imageService.getImageList(dto);
    }

    /**
     * Likes or unlikes an image.
     *
     * @param imageId image identifier
     * @param userId user identifier
     * @return true when liked after the operation
     */
    @PostMapping("/like/{imageId}")
    public Result<Boolean> likeImage(
            @PathVariable Long imageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return imageService.likeImage(userId, imageId);
    }

    /**
     * Collects or uncollects an image.
     *
     * @param imageId image identifier
     * @param userId user identifier
     * @return true when collected after the operation
     */
    @PostMapping("/collect/{imageId}")
    public Result<Boolean> collectImage(
            @PathVariable Long imageId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return imageService.collectImage(userId, imageId);
    }

    /**
     * Increments the download count and returns the image URL.
     *
     * @param imageId image identifier
     * @return image URL
     */
    @PostMapping("/download/{imageId}")
    public Result<String> downloadImage(@PathVariable Long imageId) {
        return imageService.downloadImage(imageId);
    }

    /**
     * Gets images uploaded by an author.
     *
     * @param authorId author identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return image page
     */
    @GetMapping("/author/{authorId}")
    public Result<Page<Image>> getImageByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return imageService.getImageByAuthor(authorId, pageNum, pageSize);
    }

    /**
     * Marks the image feature extraction status as completed.
     *
     * @param id image identifier
     * @return update result
     */
    @PutMapping("/{id}/feature-extracted")
    public Result<Void> markFeatureExtracted(@PathVariable Long id) {
        return imageService.markFeatureExtracted(id);
    }

    /**
     * Predicts metadata for an image before final upload.
     *
     * @param userId user identifier
     * @param file uploaded image
     * @return prediction result
     */
    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ImagePredictionDTO> predictImage(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam("file") MultipartFile file) {
        if (userId == null) {
            return unauthorized();
        }
        return imageService.predictImage(file);
    }
    private Result unauthorized() {
        return Result.fail(
                ResultCode.UNAUTHORIZED.getCode(),
                ResultCode.UNAUTHORIZED.getMsg()
        );
    }
}

package com.cr.image.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.dto.ImagePredictionDTO;
import com.cr.common.result.Result;
import com.cr.image.dto.ImageDTO;
import com.cr.image.dto.ImageQueryDTO;
import com.cr.image.entity.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image service.
 */
public interface ImageService {

    /**
     * Stores an image and creates its metadata.
     *
     * @param userId uploader identifier
     * @param file uploaded image file
     * @param dto image metadata
     * @return stored image
     */
    Result<Image> uploadImage(Long userId, MultipartFile file, ImageDTO dto);

    /**
     * Updates image metadata.
     *
     * @param imageId image identifier
     * @param dto image metadata
     * @return update result
     */
    Result<Void> updateImage(Long imageId, ImageDTO dto);

    /**
     * Deletes an image and related interaction data.
     *
     * @param imageId image identifier
     * @return deletion result
     */
    Result<Void> deleteImage(Long imageId);

    /**
     * Gets image details and increments the cached view count.
     *
     * @param imageId image identifier
     * @return image details
     */
    Result<Image> getImageById(Long imageId);

    /**
     * Gets a paginated image list.
     *
     * @param dto query parameters
     * @return image page
     */
    Result<Page<Image>> getImageList(ImageQueryDTO dto);

    /**
     * Likes or unlikes an image.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @return true when the image is liked after the operation
     */
    Result<Boolean> likeImage(Long userId, Long imageId);

    /**
     * Collects or uncollects an image.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @return true when the image is collected after the operation
     */
    Result<Boolean> collectImage(Long userId, Long imageId);

    /**
     * Increments the download count and returns the image URL.
     *
     * @param imageId image identifier
     * @return image URL
     */
    Result<String> downloadImage(Long imageId);

    /**
     * Gets images uploaded by an author.
     *
     * @param authorId author identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return image page
     */
    Result<Page<Image>> getImageByAuthor(Long authorId, Integer pageNum, Integer pageSize);

    /**
     * Marks the image feature extraction status as completed.
     *
     * @param imageId image identifier
     * @return update result
     */
    Result<Void> markFeatureExtracted(Long imageId);
    /**
     * Predicts image metadata using the recommendation service.
     *
     * @param file uploaded image file
     * @return prediction result
     */
    Result<ImagePredictionDTO> predictImage(MultipartFile file);
}

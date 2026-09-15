package com.cr.image.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.constant.CommonConstants;
import com.cr.common.dto.ImagePredictionDTO;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.image.dto.ImageDTO;
import com.cr.image.dto.ImageQueryDTO;
import com.cr.image.dto.ImageUploadDTO;
import com.cr.image.entity.Image;
import com.cr.image.entity.ImageCollect;
import com.cr.image.entity.ImageComment;
import com.cr.image.entity.ImageLike;
import com.cr.image.feign.RecommendFeignClient;
import com.cr.image.mapper.ImageCollectMapper;
import com.cr.image.mapper.ImageCommentMapper;
import com.cr.image.mapper.ImageLikeMapper;
import com.cr.image.mapper.ImageMapper;
import com.cr.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default image service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final String CONTENT_EXCHANGE = "content.exchange";

    private static final String FEATURE_EXTRACT_ROUTING_KEY = "image.feature.extract";

    private static final String NOTIFICATION_LIKE_ROUTING_KEY = "notification.like";

    private static final String NOTIFICATION_COLLECT_ROUTING_KEY = "notification.collect";

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/images/";

    private static final String VIEW_KEY_PREFIX = "image:view:";

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ImageMapper imageMapper;

    private final ImageLikeMapper imageLikeMapper;

    private final ImageCollectMapper imageCollectMapper;

    private final ImageCommentMapper imageCommentMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final RecommendFeignClient recommendFeignClient;

    @Value("${file.upload-path:D:/idea-demo/content-recommend/upload/images/}")
    private String uploadPath;

    /**
     * Stores an uploaded image locally and creates metadata.
     *
     * @param userId uploader identifier
     * @param file uploaded image
     * @param dto image metadata
     * @return stored image
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Image> uploadImage(Long userId, MultipartFile file, ImageDTO dto) {
        if (userId == null || file == null || file.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }
        if (!isImageFile(file)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "仅支持图片文件");
        }

        Path storedFile = null;
        try {
            applyPrediction(dto, file);
            ImageUploadDTO upload = storeFile(file);
            storedFile = resolveStoredFile(upload.getUrl());

            LocalDateTime now = LocalDateTime.now();
            Image image = new Image();
            image.setTitle(dto.getTitle());
            image.setDescription(dto.getDescription());
            image.setUrl(upload.getUrl());
            image.setThumbnailUrl(upload.getThumbnailUrl());
            image.setCategoryId(dto.getCategoryId());
            image.setAuthorId(userId);
            image.setAuthorName("用户" + userId);
            image.setTags(StrUtil.trim(dto.getTags()));
            image.setWidth(upload.getWidth());
            image.setHeight(upload.getHeight());
            image.setFileSize(upload.getFileSize());
            image.setViewCount(0);
            image.setLikeCount(0);
            image.setCollectCount(0);
            image.setDownloadCount(0);
            image.setFeatureExtracted(0);
            image.setStatus(CommonConstants.STATUS_ENABLED);
            image.setCreateTime(now);
            image.setUpdateTime(now);

            if (imageMapper.insert(image) != 1) {
                deleteStoredFile(storedFile);
                return Result.fail("保存图片信息失败");
            }

            sendFeatureExtractMessage(image.getId(), image.getUrl());
            return Result.success(image);
        } catch (IOException exception) {
            deleteStoredFile(storedFile);
            log.error("Failed to store uploaded image", exception);
            return Result.fail("图片上传失败");
        }
    }

    /**
     * Updates image metadata without replacing the stored file.
     *
     * @param imageId image identifier
     * @param dto image metadata
     * @return update result
     */
    @Override
    public Result<Void> updateImage(Long imageId, ImageDTO dto) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        image.setTitle(dto.getTitle());
        image.setDescription(dto.getDescription());
        image.setCategoryId(dto.getCategoryId());
        image.setTags(StrUtil.trim(dto.getTags()));
        image.setUpdateTime(LocalDateTime.now());
        return imageMapper.updateById(image) == 1
                ? Result.success()
                : Result.fail("更新图片信息失败");
    }

    /**
     * Deletes an image, its interaction records, cached count, and local file.
     *
     * @param imageId image identifier
     * @return deletion result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteImage(Long imageId) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        imageMapper.deleteById(imageId);
        imageCommentMapper.delete(new LambdaQueryWrapper<ImageComment>()
                .eq(ImageComment::getImageId, imageId));
        imageLikeMapper.delete(new LambdaQueryWrapper<ImageLike>()
                .eq(ImageLike::getImageId, imageId));
        imageCollectMapper.delete(new LambdaQueryWrapper<ImageCollect>()
                .eq(ImageCollect::getImageId, imageId));
        stringRedisTemplate.delete(viewKey(imageId));
        deleteStoredFile(resolveStoredFile(image.getUrl()));
        return Result.success();
    }

    /**
     * Gets image details and increments the Redis-backed view count.
     *
     * @param imageId image identifier
     * @return image details
     */
    @Override
    public Result<Image> getImageById(Long imageId) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        Long cachedViews = stringRedisTemplate.opsForValue().increment(viewKey(imageId));
        int baseViews = image.getViewCount() == null ? 0 : image.getViewCount();
        image.setViewCount(baseViews + (cachedViews == null ? 0 : cachedViews.intValue()));
        return Result.success(image);
    }

    /**
     * Gets a paginated list of enabled images.
     *
     * @param dto query parameters
     * @return image page
     */
    @Override
    public Result<Page<Image>> getImageList(ImageQueryDTO dto) {
        Page<Image> page = new Page<>(
                normalizePageNum(dto.getPageNum()),
                normalizePageSize(dto.getPageSize())
        );
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
                .eq(Image::getStatus, CommonConstants.STATUS_ENABLED)
                .eq(dto.getCategoryId() != null, Image::getCategoryId, dto.getCategoryId())
                .and(StrUtil.isNotBlank(dto.getKeyword()), query -> query
                        .like(Image::getTitle, dto.getKeyword())
                        .or()
                        .like(Image::getDescription, dto.getKeyword())
                        .or()
                        .like(Image::getTags, dto.getKeyword()))
                .orderByDesc(Image::getCreateTime)
                .orderByDesc(Image::getId);
        return Result.success(imageMapper.selectPage(page, wrapper));
    }

    /**
     * Toggles an image like and persists the relation.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @return true when liked after the operation
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> likeImage(Long userId, Long imageId) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        LambdaQueryWrapper<ImageLike> query = new LambdaQueryWrapper<ImageLike>()
                .eq(ImageLike::getImageId, imageId)
                .eq(ImageLike::getUserId, userId);
        if (imageLikeMapper.selectCount(query) > 0) {
            imageLikeMapper.delete(query);
            updateCount(imageId, "like_count", false);
            return Result.success(false);
        }

        ImageLike imageLike = new ImageLike();
        imageLike.setImageId(imageId);
        imageLike.setUserId(userId);
        imageLike.setCreateTime(LocalDateTime.now());
        imageLikeMapper.insert(imageLike);
        updateCount(imageId, "like_count", true);
        sendLikeMessage(userId, imageId, image.getAuthorId());
        return Result.success(true);
    }

    /**
     * Toggles an image collection and persists the relation.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @return true when collected after the operation
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> collectImage(Long userId, Long imageId) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        LambdaQueryWrapper<ImageCollect> query = new LambdaQueryWrapper<ImageCollect>()
                .eq(ImageCollect::getImageId, imageId)
                .eq(ImageCollect::getUserId, userId);
        if (imageCollectMapper.selectCount(query) > 0) {
            imageCollectMapper.delete(query);
            updateCount(imageId, "collect_count", false);
            return Result.success(false);
        }

        ImageCollect imageCollect = new ImageCollect();
        imageCollect.setImageId(imageId);
        imageCollect.setUserId(userId);
        imageCollect.setCreateTime(LocalDateTime.now());
        imageCollectMapper.insert(imageCollect);
        updateCount(imageId, "collect_count", true);
        sendCollectMessage(userId, imageId, image.getAuthorId());
        return Result.success(true);
    }

    /**
     * Increments the download count and returns the image URL.
     *
     * @param imageId image identifier
     * @return image URL
     */
    @Override
    public Result<String> downloadImage(Long imageId) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }
        updateCount(imageId, "download_count", true);
        return Result.success(image.getUrl());
    }

    /**
     * Gets a paginated list of images uploaded by an author.
     *
     * @param authorId author identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return image page
     */
    @Override
    public Result<Page<Image>> getImageByAuthor(
            Long authorId,
            Integer pageNum,
            Integer pageSize) {
        Page<Image> page = new Page<>(
                normalizePageNum(pageNum),
                normalizePageSize(pageSize)
        );
        return Result.success(imageMapper.selectPage(
                page,
                new LambdaQueryWrapper<Image>()
                        .eq(Image::getAuthorId, authorId)
                        .orderByDesc(Image::getCreateTime)
                        .orderByDesc(Image::getId)
        ));
    }

    /**
     * Predicts metadata for a temporary uploaded image.
     *
     * @param file uploaded image
     * @return prediction result
     */
    @Override
    public Result<ImagePredictionDTO> predictImage(MultipartFile file) {
        if (file == null || file.isEmpty() || !isImageFile(file)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请选择有效的图片文件");
        }
        try {
            Result<ImagePredictionDTO> result = recommendFeignClient.predictImage(file);
            if (result == null || !Integer.valueOf(200).equals(result.getCode())
                    || result.getData() == null) {
                return Result.fail("AI识别失败，请手动填写");
            }
            return result;
        } catch (Exception exception) {
            log.warn("Failed to predict uploaded image metadata", exception);
            return Result.fail("AI识别失败，请手动填写");
        }
    }

    private void applyPrediction(ImageDTO dto, MultipartFile file) {
        if (StrUtil.isNotBlank(dto.getTitle())
                && StrUtil.isNotBlank(dto.getDescription())
                && dto.getCategoryId() != null
                && StrUtil.isNotBlank(dto.getTags())) {
            return;
        }
        try {
            Result<ImagePredictionDTO> result = recommendFeignClient.predictImage(file);
            if (result == null || !Integer.valueOf(200).equals(result.getCode())
                    || result.getData() == null) {
                return;
            }
            ImagePredictionDTO prediction = result.getData();
            if (StrUtil.isBlank(dto.getTitle())) {
                dto.setTitle(prediction.getTitleSuggestion());
            }
            if (StrUtil.isBlank(dto.getDescription())) {
                dto.setDescription(prediction.getDescriptionSuggestion());
            }
            if (dto.getCategoryId() == null) {
                dto.setCategoryId(prediction.getCategoryId());
            }
            if (StrUtil.isBlank(dto.getTags())) {
                dto.setTags(prediction.getTagsSuggestion());
            }
        } catch (Exception exception) {
            log.warn("AI metadata prediction failed for uploaded image", exception);
        }
    }
    /**
     * Marks the image feature extraction status as completed.
     *
     * @param imageId image identifier
     * @return update result
     */
    @Override
    public Result<Void> markFeatureExtracted(Long imageId) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }
        image.setFeatureExtracted(1);
        image.setUpdateTime(LocalDateTime.now());
        return imageMapper.updateById(image) == 1
                ? Result.success()
                : Result.fail("更新特征提取状态失败");
    }

    private ImageUploadDTO storeFile(MultipartFile file) throws IOException {
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        String datePath = LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );
        Path directory = root.resolve(datePath).normalize();
        if (!directory.startsWith(root)) {
            throw new IOException("Invalid upload directory");
        }
        Files.createDirectories(directory);

        String extension = fileExtension(file.getOriginalFilename());
        Path target = directory.resolve(UUID.randomUUID() + extension);
        file.transferTo(target.toFile());

        BufferedImage bufferedImage = ImageIO.read(target.toFile());
        ImageUploadDTO upload = new ImageUploadDTO();
        upload.setUrl(PUBLIC_UPLOAD_PREFIX
                + root.relativize(target).toString().replace('\\', '/'));
        upload.setThumbnailUrl(upload.getUrl());
        upload.setWidth(bufferedImage == null ? null : bufferedImage.getWidth());
        upload.setHeight(bufferedImage == null ? null : bufferedImage.getHeight());
        upload.setFileSize(file.getSize());
        return upload;
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    private String fileExtension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (StrUtil.isBlank(extension)) {
            return ".jpg";
        }
        return "." + extension.toLowerCase();
    }

    private Path resolveStoredFile(String url) {
        if (StrUtil.isBlank(url) || !url.startsWith(PUBLIC_UPLOAD_PREFIX)) {
            return null;
        }
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = root.resolve(url.substring(PUBLIC_UPLOAD_PREFIX.length()))
                .normalize();
        return target.startsWith(root) ? target : null;
    }

    private void deleteStoredFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("Failed to delete stored image {}", path, exception);
        }
    }

    private void updateCount(Long imageId, String column, boolean increase) {
        String expression = increase
                ? column + " = COALESCE(" + column + ", 0) + 1"
                : column + " = GREATEST(COALESCE(" + column + ", 0) - 1, 0)";
        imageMapper.update(null, new LambdaUpdateWrapper<Image>()
                .eq(Image::getId, imageId)
                .setSql(expression));
    }

    private void sendFeatureExtractMessage(Long imageId, String imageUrl) {
        Map<String, Object> message = new HashMap<>();
        message.put("imageId", imageId);
        message.put("imageUrl", imageUrl);
        try {
            rabbitTemplate.convertAndSend(
                    CONTENT_EXCHANGE,
                    FEATURE_EXTRACT_ROUTING_KEY,
                    message
            );
        } catch (AmqpException exception) {
            log.warn(
                    "Failed to send feature extraction message for image {}",
                    imageId,
                    exception
            );
        }
    }

    private void sendLikeMessage(Long userId, Long imageId, Long authorId) {
        Map<String, Object> message = new HashMap<>();
        message.put("userId", userId);
        message.put("imageId", imageId);
        message.put("authorId", authorId);
        message.put("type", "like");
        try {
            rabbitTemplate.convertAndSend(
                    CONTENT_EXCHANGE,
                    NOTIFICATION_LIKE_ROUTING_KEY,
                    message
            );
        } catch (AmqpException exception) {
            log.warn(
                    "Failed to send like notification for image {} and user {}",
                    imageId,
                    userId,
                    exception
            );
        }
    }

    private void sendCollectMessage(Long userId, Long imageId, Long authorId) {
        Map<String, Object> message = new HashMap<>();
        message.put("userId", userId);
        message.put("imageId", imageId);
        message.put("authorId", authorId);
        message.put("type", "collect");
        try {
            rabbitTemplate.convertAndSend(
                    CONTENT_EXCHANGE,
                    NOTIFICATION_COLLECT_ROUTING_KEY,
                    message
            );
        } catch (AmqpException exception) {
            log.warn(
                    "Failed to send collect notification for image {} and user {}",
                    imageId,
                    userId,
                    exception
            );
        }
    }

    private String viewKey(Long imageId) {
        return CommonConstants.REDIS_CONTENT_PREFIX + VIEW_KEY_PREFIX + imageId;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}

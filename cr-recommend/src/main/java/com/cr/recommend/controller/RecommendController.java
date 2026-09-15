package com.cr.recommend.controller;

import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.recommend.dto.BehaviorDTO;
import com.cr.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Recommendation API.
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * Gets mixed recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @GetMapping("/get")
    public Result getRecommend(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam(required = false) Integer count) {
        if (userId == null) {
            return recommendService.getHotRecommend(count);
        }
        return recommendService.getRecommend(userId, count);
    }

    /**
     * Gets image-content recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @GetMapping("/content")
    public Result getContentBasedRecommend(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam(required = false) Integer count) {
        if (userId == null) {
            return recommendService.getHotRecommend(count);
        }
        return recommendService.getContentBasedRecommend(userId, count);
    }

    /**
     * Gets collaborative-filtering recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @GetMapping("/cf")
    public Result getCFRecommend(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam(required = false) Integer count) {
        if (userId == null) {
            return recommendService.getHotRecommend(count);
        }
        return recommendService.getCFRecommend(userId, count);
    }

    /**
     * Gets hot recommendations.
     *
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @GetMapping("/hot")
    public Result getHotRecommend(
            @RequestParam(required = false) Integer count) {
        return recommendService.getHotRecommend(count);
    }

    /**
     * Records a user behavior.
     *
     * @param userId user identifier
     * @param userId user identifier
     * @param dto behavior request
     * @return recording result
     */
    @PostMapping("/behavior")
    public Result recordBehavior(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestBody(required = false) BehaviorDTO dto) {
        if (userId == null) {
            return unauthorized();
        }
        if (dto == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求参数不能为空");
        }
        return recommendService.recordBehavior(
                userId,
                dto.getImageId(),
                dto.getBehaviorType(),
                dto.getDuration()
        );
    }

    /**
     * Asynchronously refreshes recommendations for a user.
     *
     * @param userId user identifier
     * @return refresh result
     */
    @PostMapping("/refresh")
    public Result refreshRecommend(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        recommendService.refreshRecommend(userId);
        return Result.success("推荐刷新任务已提交");
    }

    /**
     * Manually extracts and stores an image feature vector.
     *
     * @param imageId image identifier
     * @return feature dimension
     */
    @PostMapping("/feature/extract/{imageId}")
    public Result extractFeature(@PathVariable Long imageId) {
        return recommendService.extractFeature(imageId);
    }

    /**
     * Initializes Milvus and imports features for existing images.
     *
     * @return imported image count
     */
    @PostMapping("/milvus/init")
    public Result initMilvus() {
        return recommendService.initMilvus();
    }

    /**
     * Predicts image category and descriptive metadata.
     *
     * @param file uploaded image file
     * @return prediction result
     */
    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result predictImage(@RequestParam("file") MultipartFile file) {
        return recommendService.predictImage(file);
    }

    /**
     * Gets images similar to the specified image.
     *
     * @param imageId image identifier
     * @param count result count
     * @return similar image identifiers
     */
    @GetMapping("/content/{imageId}")
    public Result getSimilarImages(
            @PathVariable Long imageId,
            @RequestParam(required = false, defaultValue = "12") Integer count) {
        return recommendService.getSimilarImages(imageId, count);
    }
    private Result unauthorized() {
        return Result.fail(
                ResultCode.UNAUTHORIZED.getCode(),
                ResultCode.UNAUTHORIZED.getMsg()
        );
    }
}

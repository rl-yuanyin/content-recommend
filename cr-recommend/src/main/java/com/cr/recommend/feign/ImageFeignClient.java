package com.cr.recommend.feign;

import com.cr.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Feign client for the image service.
 */
@FeignClient(name = "cr-image")
public interface ImageFeignClient {

    /**
     * Marks an image feature as extracted.
     *
     * @param imageId image identifier
     * @return update result
     */
    @PutMapping("/api/image/{id}/feature-extracted")
    Result<Void> updateFeatureExtracted(@PathVariable("id") Long imageId);
}

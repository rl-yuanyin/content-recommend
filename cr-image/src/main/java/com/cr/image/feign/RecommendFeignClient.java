package com.cr.image.feign;

import com.cr.common.dto.ImagePredictionDTO;
import com.cr.common.result.Result;
import org.springframework.http.MediaType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * Feign client for recommendation and image prediction APIs.
 */
@FeignClient(name = "cr-recommend")
public interface RecommendFeignClient {

    /**
     * Predicts image metadata from uploaded image bytes.
     *
     * @param file image file
     * @return prediction result
     */
    @PostMapping(
            value = "/api/recommend/predict",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<ImagePredictionDTO> predictImage(@RequestPart("file") MultipartFile file);
}

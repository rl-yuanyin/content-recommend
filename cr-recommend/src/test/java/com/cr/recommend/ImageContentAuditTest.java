package com.cr.recommend;

import ai.djl.Application;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Temporary audit utility for validating Pexels image content.
 */
class ImageContentAuditTest {

    private static final String IMAGE_DIRECTORY =
            "D:/idea-demo/content-recommend/.pexels_audit/candidates";

    @Test
    void auditImageContent() throws Exception {
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.pytorch/resnet")
                .optFilter("layers", "50")
                .build();

        try (ZooModel<Image, Classifications> model = criteria.loadModel();
                Predictor<Image, Classifications> predictor = model.newPredictor()) {
            List<Path> images = Files.list(Paths.get(IMAGE_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".jpg"))
                    .sorted(Comparator.comparingInt(path -> imageId(path)))
                    .collect(Collectors.toList());

            for (Path imagePath : images) {
                Image image = ImageFactory.getInstance().fromFile(imagePath);
                Classifications classifications = predictor.predict(image);
                String labels = classifications.topK(5).stream()
                        .map(item -> item.getClassName()
                                + "="
                                + String.format("%.4f", item.getProbability()))
                        .collect(Collectors.joining(" | "));
                System.out.println("AUDIT_IMAGE|" + imageId(imagePath) + "|" + labels);
            }
        }
    }

    private int imageId(Path path) {
        String name = path.getFileName().toString();
        return Integer.parseInt(name.substring(0, name.length() - 4));
    }
}

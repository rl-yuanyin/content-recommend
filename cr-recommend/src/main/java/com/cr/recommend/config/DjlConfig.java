package com.cr.recommend.config;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.ImageFeatureExtractor;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DJL ResNet50 image feature extraction configuration.
 *
 * <p>DJL 0.27 does not expose an IMAGE_FEATURE_EXTRACTION application constant.
 * The ResNet50 pretrained backbone and DJL's ImageFeatureExtractor are therefore
 * combined with a translator that returns the deep feature array directly.</p>
 */
@Configuration
public class DjlConfig {

    private static final Application IMAGE_FEATURE_EXTRACTION =
            Application.of("cv/image_feature_extraction");

    /**
     * Loads the pretrained ResNet50 model.
     *
     * @return ResNet50 feature extraction model
     * @throws IOException when the model cannot be downloaded
     * @throws ModelNotFoundException when no matching model is found
     * @throws MalformedModelException when the model is invalid
     */
    @Bean(destroyMethod = "close")
    public ZooModel<Image, float[]> resNet50Model()
            throws IOException, ModelNotFoundException, MalformedModelException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("width", 224);
        arguments.put("height", 224);
        arguments.put("resize", 256);
        arguments.put("centerCrop", true);
        arguments.put("normalize", true);
        arguments.put("applySoftmax", false);

        ImageFeatureExtractor extractor =
                ImageFeatureExtractor.builder(arguments).build();
        Translator<Image, float[]> translator = new ResNet50FeatureTranslator(extractor);

        Criteria<Image, float[]> criteria = Criteria.builder()
                .setTypes(Image.class, float[].class)
                .optApplication(IMAGE_FEATURE_EXTRACTION)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.pytorch/resnet")
                .optFilter("layers", "50")
                .optTranslator(translator)
                .build();
        return criteria.loadModel();
    }

    /**
     * Adapts DJL's image preprocessing to a float feature output.
     */
    private static final class ResNet50FeatureTranslator
            implements Translator<Image, float[]> {

        private final ImageFeatureExtractor delegate;

        private ResNet50FeatureTranslator(ImageFeatureExtractor delegate) {
            this.delegate = delegate;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, Image input) {
            return delegate.processInput(ctx, input);
        }

        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {
            return list.get(0).toFloatArray();
        }

        @Override
        public Batchifier getBatchifier() {
            return delegate.getBatchifier();
        }

        @Override
        public void prepare(TranslatorContext ctx) throws Exception {
            delegate.prepare(ctx);
        }
    }
}

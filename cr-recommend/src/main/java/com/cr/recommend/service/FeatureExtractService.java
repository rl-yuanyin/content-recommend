package com.cr.recommend.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ZooModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Deep image feature extraction service based on DJL and ResNet50.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractService {

    private static final String LOCAL_UPLOAD_PREFIX = "/uploads/images/";

    private final ZooModel<Image, float[]> resNet50Model;

    @Value("${djl.feature-dim:2048}")
    private int featureDimension;

    @Value("${file.upload-path:D:/idea-demo/content-recommend/upload/images/}")
    private String uploadPath;

    @Value("${djl.http-proxy-host:}")
    private String proxyHost;

    @Value("${djl.http-proxy-port:0}")
    private int proxyPort;

    @Value("${djl.connect-timeout-ms:15000}")
    private int connectTimeoutMs;

    @Value("${djl.read-timeout-ms:60000}")
    private int readTimeoutMs;

    private Predictor<Image, float[]> predictor;

    @PostConstruct
    public void initializePredictor() {
        predictor = resNet50Model.newPredictor();
    }

    /**
     * Extracts a normalized feature vector from an image URL or local upload URL.
     *
     * @param imageUrl image URL
     * @return 2048-dimensional feature vector, or null on failure
     */
    public float[] extractFeature(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        try {
            Image image;
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                image = downloadRemoteImage(imageUrl);
            } else {
                image = ImageFactory.getInstance().fromFile(resolveLocalPath(imageUrl));
            }
            return predict(image);
        } catch (Exception exception) {
            log.warn("Failed to extract image feature from {}", imageUrl, exception);
            return null;
        }
    }

    /**
     * Downloads a remote image with an explicit HTTP proxy and generous timeouts.
     *
     * <p>DJL's ImageFactory.fromUrl implementation does not expose a proxy
     * parameter. Downloading first also makes connection and TLS failures
     * visible in the service logs.</p>
     *
     * @param imageUrl remote image URL
     * @return decoded image
     * @throws IOException when both proxy and direct downloads fail
     */
    private Image downloadRemoteImage(String imageUrl) throws IOException {
        Proxy proxy = resolveProxy();
        IOException proxyFailure = null;
        if (proxy != null) {
            try {
                log.info(
                        "Downloading image through proxy {}:{}: {}",
                        proxyHost,
                        proxyPort,
                        imageUrl
                );
                return downloadImage(imageUrl, proxy);
            } catch (IOException exception) {
                proxyFailure = exception;
                log.warn(
                        "Proxy image download failed, trying direct connection: {}",
                        imageUrl,
                        exception
                );
            }
        }

        try {
            log.info("Downloading image directly: {}", imageUrl);
            return downloadImage(imageUrl, Proxy.NO_PROXY);
        } catch (IOException directFailure) {
            if (proxyFailure != null) {
                directFailure.addSuppressed(proxyFailure);
            }
            throw directFailure;
        }
    }

    /**
     * Downloads and decodes one image using the supplied proxy.
     *
     * @param imageUrl remote image URL
     * @param proxy HTTP proxy or NO_PROXY
     * @return decoded image
     * @throws IOException when the request or image decoding fails
     */
    private Image downloadImage(String imageUrl, Proxy proxy) throws IOException {
        if (imageUrl.contains("pexels.com") && !imageUrl.contains("fm=jpg")) {
            imageUrl = imageUrl + (imageUrl.contains("?") ? "&" : "?") + "fm=jpg";
        }
        URLConnection connection = new URL(imageUrl).openConnection(proxy);
        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("Unsupported image URL protocol");
        }

        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setConnectTimeout(connectTimeoutMs);
        httpConnection.setReadTimeout(readTimeoutMs);
        httpConnection.setInstanceFollowRedirects(true);
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 Chrome/120 Safari/537.36"
        );
        httpConnection.setRequestProperty(
                "Accept",
                "image/jpeg,image/jpg,image/png,image/*;q=0.8"
        );
        httpConnection.setRequestProperty("Referer", "https://www.pexels.com/");
        httpConnection.setRequestProperty("Connection", "close");

        int statusCode = httpConnection.getResponseCode();
        if (statusCode < 200 || statusCode >= 300) {
            httpConnection.disconnect();
            throw new IOException("Image HTTP status: " + statusCode);
        }

        try (InputStream inputStream = httpConnection.getInputStream()) {
            return ImageFactory.getInstance().fromInputStream(inputStream);
        } finally {
            httpConnection.disconnect();
        }
    }

    /**
     * Resolves the HTTP proxy from Spring configuration, then from JVM flags.
     *
     * @return configured proxy or null for direct-only mode
     */
    private Proxy resolveProxy() {
        String host = proxyHost;
        int port = proxyPort;

        if (isBlank(host)) {
            host = System.getProperty("https.proxyHost");
        }
        if (port <= 0) {
            port = parsePort(System.getProperty("https.proxyPort"));
        }
        if (isBlank(host)) {
            host = System.getProperty("http.proxyHost");
        }
        if (port <= 0) {
            port = parsePort(System.getProperty("http.proxyPort"));
        }
        if (isBlank(host) || port <= 0) {
            return null;
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    private int parsePort(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Extracts a normalized feature vector from an input stream.
     *
     * @param inputStream image input stream
     * @return 2048-dimensional feature vector, or null on failure
     */
    public float[] extractFeatureFromInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            Image image = ImageFactory.getInstance().fromInputStream(inputStream);
            return predict(image);
        } catch (Exception exception) {
            log.warn("Failed to extract image feature from input stream", exception);
            return null;
        }
    }

    /**
     * Extracts a normalized feature vector from a local file.
     *
     * @param filePath local image path
     * @return 2048-dimensional feature vector, or null on failure
     */
    public float[] extractFeatureFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        try {
            Image image = ImageFactory.getInstance().fromFile(Paths.get(filePath));
            return predict(image);
        } catch (Exception exception) {
            log.warn("Failed to extract image feature from file {}", filePath, exception);
            return null;
        }
    }

    /**
     * Runs synchronized inference because a DJL Predictor is not thread-safe.
     *
     * @param image input image
     * @return normalized feature vector
     * @throws Exception when inference fails
     */
    private float[] predict(Image image) throws Exception {
        float[] rawFeature;
        synchronized (predictor) {
            rawFeature = predictor.predict(image);
        }
        if (rawFeature == null || rawFeature.length == 0) {
            return null;
        }
        return normalizeFeature(rawFeature);
    }

    /**
     * Normalizes the model output to the configured feature dimension.
     *
     * @param rawFeature raw model output
     * @return feature vector with the configured dimension
     */
    private float[] normalizeFeature(float[] rawFeature) {
        int dimension = featureDimension > 0 ? featureDimension : 2048;
        float[] feature = new float[dimension];
        System.arraycopy(rawFeature, 0, feature, 0, Math.min(rawFeature.length, dimension));

        double norm = 0.0D;
        for (float value : feature) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm > 0.0D) {
            for (int index = 0; index < feature.length; index++) {
                feature[index] = (float) (feature[index] / norm);
            }
        }
        return feature;
    }

    /**
     * Resolves a public upload URL to the local upload directory.
     *
     * @param imageUrl public upload URL
     * @return local image path
     */
    private Path resolveLocalPath(String imageUrl) {
        String relative = imageUrl.startsWith(LOCAL_UPLOAD_PREFIX)
                ? imageUrl.substring(LOCAL_UPLOAD_PREFIX.length())
                : imageUrl;
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path path = root.resolve(relative).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new IllegalArgumentException("Image file does not exist: " + imageUrl);
        }
        return path;
    }

    @PreDestroy
    public void closePredictor() {
        if (predictor != null) {
            predictor.close();
        }
    }
}

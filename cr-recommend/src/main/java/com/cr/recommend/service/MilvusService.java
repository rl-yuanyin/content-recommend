package com.cr.recommend.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Milvus vector storage and approximate nearest-neighbor search service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusService {

    private static final String IMAGE_ID_FIELD = "image_id";

    private static final String VECTOR_FIELD = "vector";

    private static final String CATEGORY_ID_FIELD = "category_id";

    private static final Gson GSON = new Gson();

    private final MilvusClientV2 milvusClientV2;

    @Value("${milvus.collection-name:image_features}")
    private String collectionName;

    @Value("${djl.feature-dim:2048}")
    private int featureDimension;

    /**
     * Attempts initialization after startup without preventing service startup
     * when Milvus is temporarily unavailable.
     */
    @PostConstruct
    public void initializeAfterStartup() {
        try {
            initCollection();
        } catch (Exception exception) {
            log.warn(
                    "Milvus is not available during startup. "
                            + "Call /api/recommend/milvus/init after Milvus starts.",
                    exception
            );
        }
    }

    /**
     * Creates the image feature collection and IVF_FLAT index when missing.
     */
    public synchronized void initCollection() {
        Boolean exists = milvusClientV2.hasCollection(
                HasCollectionReq.builder()
                        .collectionName(collectionName)
                        .build()
        );
        if (Boolean.TRUE.equals(exists)) {
            loadCollection();
            return;
        }

        List<CreateCollectionReq.FieldSchema> fields = new ArrayList<>();
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(IMAGE_ID_FIELD)
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(false)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(VECTOR_FIELD)
                .dataType(DataType.FloatVector)
                .dimension(featureDimension)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(CATEGORY_ID_FIELD)
                .dataType(DataType.Int64)
                .build());

        CreateCollectionReq.CollectionSchema schema =
                CreateCollectionReq.CollectionSchema.builder()
                        .fieldSchemaList(fields)
                        .build();
        milvusClientV2.createCollection(
                CreateCollectionReq.builder()
                        .collectionName(collectionName)
                        .collectionSchema(schema)
                        .build()
        );

        Map<String, Object> indexParams = new HashMap<>();
        indexParams.put("nlist", 1024);
        IndexParam indexParam = IndexParam.builder()
                .fieldName(VECTOR_FIELD)
                .indexName("idx_image_feature")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.COSINE)
                .extraParams(indexParams)
                .build();
        milvusClientV2.createIndex(
                CreateIndexReq.builder()
                        .collectionName(collectionName)
                        .indexParams(Collections.singletonList(indexParam))
                        .build()
        );
        loadCollection();
        log.info("Milvus collection {} initialized", collectionName);
    }

    /**
     * Inserts or replaces an image feature vector.
     *
     * @param imageId image identifier
     * @param vector 2048-dimensional feature vector
     * @param categoryId image category identifier
     */
    public synchronized void insertVector(Long imageId, float[] vector, Long categoryId) {
        validateVector(vector);
        deleteVector(imageId);

        JsonObject row = new JsonObject();
        row.addProperty(IMAGE_ID_FIELD, imageId);
        row.add(VECTOR_FIELD, GSON.toJsonTree(vector));
        row.addProperty(CATEGORY_ID_FIELD, categoryId == null ? 0L : categoryId);
        milvusClientV2.insert(
                InsertReq.builder()
                        .collectionName(collectionName)
                        .data(Collections.singletonList(row))
                        .build()
        );
    }

    /**
     * Searches similar images using cosine distance.
     *
     * @param vector query vector
     * @param topK result count
     * @param categoryId optional category filter
     * @return similar image identifiers
     */
    public List<Long> searchSimilar(float[] vector, int topK, Long categoryId) {
        validateVector(vector);
        if (topK <= 0) {
            return Collections.emptyList();
        }

        List<BaseVector> queryVectors = Collections.<BaseVector>singletonList(
                new FloatVec(vector)
        );
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(collectionName)
                .annsField(VECTOR_FIELD)
                .topK(topK)
                .data(queryVectors)
                .outputFields(Collections.singletonList(IMAGE_ID_FIELD))
                .searchParams(Collections.<String, Object>singletonMap("nprobe", 16));
        if (categoryId != null) {
            builder.filter(CATEGORY_ID_FIELD + " == " + categoryId);
        }

        SearchResp response = milvusClientV2.search(builder.build());
        List<List<SearchResp.SearchResult>> searchResults = response.getSearchResults();
        if (searchResults == null || searchResults.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> imageIds = new ArrayList<>();
        for (SearchResp.SearchResult result : searchResults.get(0)) {
            Long imageId = longValue(result.getId());
            if (imageId != null) {
                imageIds.add(imageId);
            }
        }
        return imageIds;
    }

    /**
     * Loads an image feature vector by image ID.
     *
     * @param imageId image identifier
     * @return feature vector or null
     */
    public float[] getVector(Long imageId) {
        QueryResp response = milvusClientV2.query(
                QueryReq.builder()
                        .collectionName(collectionName)
                        .ids(Collections.<Object>singletonList(imageId))
                        .outputFields(Collections.singletonList(VECTOR_FIELD))
                        .build()
        );
        if (response.getQueryResults() == null
                || response.getQueryResults().isEmpty()) {
            return null;
        }
        Object value = response.getQueryResults().get(0).getEntity().get(VECTOR_FIELD);
        if (value instanceof float[]) {
            return (float[]) value;
        }
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            float[] vector = new float[values.size()];
            for (int index = 0; index < values.size(); index++) {
                vector[index] = ((Number) values.get(index)).floatValue();
            }
            return vector;
        }
        return null;
    }

    /**
     * Deletes an image feature vector.
     *
     * @param imageId image identifier
     */
    public synchronized void deleteVector(Long imageId) {
        milvusClientV2.delete(
                DeleteReq.builder()
                        .collectionName(collectionName)
                        .ids(Collections.<Object>singletonList(imageId))
                        .build()
        );
    }

    /**
     * Loads the collection into memory.
     */
    public void loadCollection() {
        milvusClientV2.loadCollection(
                LoadCollectionReq.builder()
                        .collectionName(collectionName)
                        .build()
        );
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != featureDimension) {
            throw new IllegalArgumentException(
                    "Feature vector dimension must be " + featureDimension
            );
        }
    }

    private Long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.valueOf(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @PreDestroy
    public void closeClient() {
        try {
            milvusClientV2.close(5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}

package com.cr.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Image metadata entity.
 */
@Data
@TableName("image")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("url")
    private String url;

    @TableField("thumbnail_url")
    private String thumbnailUrl;

    @TableField("category_id")
    private Long categoryId;

    @TableField("author_id")
    private Long authorId;

    @TableField("author_name")
    private String authorName;

    /**
     * Comma-separated image tags.
     */
    @TableField("tags")
    private String tags;

    @TableField("width")
    private Integer width;

    @TableField("height")
    private Integer height;

    @TableField("file_size")
    private Long fileSize;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("collect_count")
    private Integer collectCount;

    @TableField("download_count")
    private Integer downloadCount;

    /**
     * Feature extraction status. 0 not extracted, 1 extracted.
     */
    @TableField("feature_extracted")
    private Integer featureExtracted;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

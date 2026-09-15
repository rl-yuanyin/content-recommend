package com.cr.recommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Generated recommendation result entity.
 */
@Data
@TableName("recommend_result")
public class RecommendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /**
     * Comma-separated recommended image identifiers.
     */
    @TableField("image_ids")
    private String imageIds;

    /**
     * Algorithm type: image_content, cf, hot, or mixed.
     */
    @TableField("algorithm_type")
    private String algorithmType;

    @TableField("create_time")
    private LocalDateTime createTime;
}

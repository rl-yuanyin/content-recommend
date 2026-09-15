package com.cr.recommend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User-image behavior entity.
 */
@Data
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("image_id")
    private Long imageId;

    /**
     * Behavior type. 1 view, 2 like, 3 comment, 5 collect, 6 download.
     */
    @TableField("behavior_type")
    private Integer behaviorType;

    /**
     * Dwell time in seconds.
     */
    @TableField("duration")
    private Integer duration;

    @TableField("create_time")
    private LocalDateTime createTime;
}

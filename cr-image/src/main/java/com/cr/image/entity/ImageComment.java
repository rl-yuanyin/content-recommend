package com.cr.image.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Image comment entity.
 */
@Data
@TableName("image_comment")
public class ImageComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("image_id")
    private Long imageId;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("content")
    private String content;

    @TableField("parent_id")
    private Long parentId;

    @TableField("create_time")
    private LocalDateTime createTime;
}

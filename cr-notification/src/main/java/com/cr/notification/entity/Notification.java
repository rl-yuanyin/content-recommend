package com.cr.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Notification entity.
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /**
     * Notification type. 1 like, 2 comment, 3 collect, 4 system.
     */
    @TableField("type")
    private Integer type;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("related_id")
    private Long relatedId;

    /**
     * Read status. 0 unread, 1 read.
     */
    @TableField("is_read")
    private Integer isRead;

    @TableField("create_time")
    private LocalDateTime createTime;
}

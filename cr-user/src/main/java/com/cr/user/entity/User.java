package com.cr.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User account entity.
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Login username.
     */
    @TableField("username")
    private String username;

    /**
     * BCrypt password hash.
     */
    @TableField("password")
    private String password;

    /**
     * Display nickname.
     */
    @TableField("nickname")
    private String nickname;

    /**
     * Avatar URL.
     */
    @TableField("avatar")
    private String avatar;

    /**
     * Email address.
     */
    @TableField("email")
    private String email;

    /**
     * Mobile phone number.
     */
    @TableField("phone")
    private String phone;

    /**
     * Account status. 0 disabled, 1 enabled.
     */
    @TableField("status")
    private Integer status;

    /**
     * Creation time.
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * Last update time.
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}

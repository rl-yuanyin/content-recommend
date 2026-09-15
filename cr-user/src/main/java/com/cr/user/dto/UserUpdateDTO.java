package com.cr.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User profile update request.
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;
}

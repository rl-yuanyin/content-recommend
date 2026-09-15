package com.cr.user.service;

import com.cr.common.result.Result;
import com.cr.user.dto.LoginDTO;
import com.cr.user.dto.RegisterDTO;
import com.cr.user.dto.UserUpdateDTO;
import com.cr.user.entity.User;

/**
 * User account service.
 */
public interface UserService {

    /**
     * Authenticates a user and returns an access token.
     *
     * @param dto login request
     * @return access token result
     */
    Result<String> login(LoginDTO dto);

    /**
     * Registers a new user.
     *
     * @param dto registration request
     * @return registration result
     */
    Result<Void> register(RegisterDTO dto);

    /**
     * Gets user information by identifier.
     *
     * @param userId user identifier
     * @return user information
     */
    Result<User> getUserInfo(Long userId);

    /**
     * Updates user profile information.
     *
     * @param userId user identifier
     * @param dto profile update request
     * @return update result
     */
    Result<Void> updateUserInfo(Long userId, UserUpdateDTO dto);

    /**
     * Logs out a user and invalidates the cached token.
     *
     * @param userId user identifier
     * @return logout result
     */
    Result<Void> logout(Long userId);
}

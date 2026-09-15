package com.cr.user.controller;

import com.cr.common.constant.CommonConstants;
import com.cr.common.exception.BusinessException;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.common.util.JwtUtil;
import com.cr.user.dto.LoginDTO;
import com.cr.user.dto.RegisterDTO;
import com.cr.user.dto.UserUpdateDTO;
import com.cr.user.entity.User;
import com.cr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * User account API.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Authenticates a user.
     *
     * @param dto login request
     * @return access token
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    /**
     * Registers a user account.
     *
     * @param dto registration request
     * @return registration result
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    /**
     * Gets the current user's information.
     *
     * @param authorization access token
     * @return user information
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(
            @RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false)
            String authorization) {
        return userService.getUserInfo(resolveUserId(authorization));
    }

    /**
     * Updates the current user's information.
     *
     * @param authorization access token
     * @param dto profile update request
     * @return update result
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(
            @RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false)
            String authorization,
            @Valid @RequestBody UserUpdateDTO dto) {
        return userService.updateUserInfo(resolveUserId(authorization), dto);
    }

    /**
     * Logs out the current user.
     *
     * @param authorization access token
     * @return logout result
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false)
            String authorization) {
        return userService.logout(resolveUserId(authorization));
    }

    /**
     * Resolves the user identifier from a Bearer token or raw JWT.
     *
     * @param authorization authorization header value
     * @return user identifier
     */
    private Long resolveUserId(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new BusinessException(
                    ResultCode.UNAUTHORIZED.getCode(),
                    ResultCode.UNAUTHORIZED.getMsg()
            );
        }

        String token = authorization.trim();
        if (token.regionMatches(
                true,
                0,
                CommonConstants.TOKEN_PREFIX,
                0,
                CommonConstants.TOKEN_PREFIX.length())) {
            token = token.substring(CommonConstants.TOKEN_PREFIX.length()).trim();
        }

        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(
                    ResultCode.UNAUTHORIZED.getCode(),
                    ResultCode.UNAUTHORIZED.getMsg()
            );
        }
        return JwtUtil.parseToken(token);
    }
}

package com.cr.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cr.common.constant.CommonConstants;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.common.util.JwtUtil;
import com.cr.user.dto.LoginDTO;
import com.cr.user.dto.RegisterDTO;
import com.cr.user.dto.UserUpdateDTO;
import com.cr.user.entity.User;
import com.cr.user.mapper.UserMapper;
import com.cr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Default user account service implementation.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticates a user and caches the generated token for 24 hours.
     *
     * @param dto login request
     * @return access token result
     */
    @Override
    public Result<String> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        if (!Integer.valueOf(CommonConstants.STATUS_ENABLED).equals(user.getStatus())) {
            return Result.fail(ResultCode.FORBIDDEN.getCode(), "用户已被禁用");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        stringRedisTemplate.opsForValue().set(
                CommonConstants.REDIS_TOKEN_PREFIX + user.getId(),
                token,
                24,
                TimeUnit.HOURS
        );
        return Result.success(token);
    }

    /**
     * Registers a new enabled user with a BCrypt password hash.
     *
     * @param dto registration request
     * @return registration result
     */
    @Override
    public Result<Void> register(RegisterDTO dto) {
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (userCount > 0) {
            return Result.fail("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StrUtil.isBlank(dto.getNickname())
                ? dto.getUsername()
                : dto.getNickname());
        user.setStatus(CommonConstants.STATUS_ENABLED);
        user.setCreateTime(now);
        user.setUpdateTime(now);

        if (userMapper.insert(user) != 1) {
            return Result.fail("注册失败");
        }
        return Result.success();
    }

    /**
     * Gets user information without exposing the password hash.
     *
     * @param userId user identifier
     * @return user information
     */
    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * Updates mutable profile fields.
     *
     * @param userId user identifier
     * @param dto profile update request
     * @return update result
     */
    @Override
    public Result<Void> updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        user.setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) != 1) {
            return Result.fail("更新用户信息失败");
        }
        return Result.success();
    }

    /**
     * Removes the cached access token for a user.
     *
     * @param userId user identifier
     * @return logout result
     */
    @Override
    public Result<Void> logout(Long userId) {
        stringRedisTemplate.delete(CommonConstants.REDIS_TOKEN_PREFIX + userId);
        return Result.success();
    }
}

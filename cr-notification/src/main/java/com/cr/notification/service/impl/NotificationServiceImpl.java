package com.cr.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.notification.constant.NotificationConstants;
import com.cr.notification.entity.Notification;
import com.cr.notification.mapper.NotificationMapper;
import com.cr.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Default notification service implementation.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private static final long UNREAD_CACHE_HOURS = 24L;

    private final NotificationMapper notificationMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Gets notifications ordered by creation time descending.
     *
     * @param userId user identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return notification page
     */
    @Override
    public Result getNotificationList(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        Page<Notification> page = new Page<>(
                normalizePageNum(pageNum),
                normalizePageSize(pageSize)
        );
        Page<Notification> result = notificationMapper.selectPage(
                page,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime)
                        .orderByDesc(Notification::getId)
        );
        return Result.success(result);
    }

    /**
     * Gets the unread count from Redis, loading it from MySQL on cache misses.
     *
     * @param userId user identifier
     * @return unread count
     */
    @Override
    public Result getUnreadCount(Long userId) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        String cacheKey = unreadCacheKey(userId);
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            try {
                return Result.success(Long.valueOf(cachedValue));
            } catch (NumberFormatException ignored) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, NotificationConstants.UNREAD)
        );
        stringRedisTemplate.opsForValue().set(
                cacheKey,
                String.valueOf(unreadCount),
                UNREAD_CACHE_HOURS,
                TimeUnit.HOURS
        );
        return Result.success(unreadCount);
    }

    /**
     * Marks one notification as read.
     *
     * @param userId user identifier
     * @param notificationId notification identifier
     * @return update result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result markAsRead(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "通知参数不完整");
        }

        int updated = notificationMapper.update(
                null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, NotificationConstants.UNREAD)
                        .set(Notification::getIsRead, NotificationConstants.READ)
        );
        if (updated == 0) {
            Notification notification = notificationMapper.selectOne(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getId, notificationId)
                            .eq(Notification::getUserId, userId)
            );
            if (notification == null) {
                return Result.fail(ResultCode.NOT_FOUND.getCode(), "通知不存在");
            }
        }

        stringRedisTemplate.delete(unreadCacheKey(userId));
        return Result.success();
    }

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId user identifier
     * @return update result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result markAllAsRead(Long userId) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        notificationMapper.update(
                null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, NotificationConstants.UNREAD)
                        .set(Notification::getIsRead, NotificationConstants.READ)
        );
        stringRedisTemplate.delete(unreadCacheKey(userId));
        return Result.success();
    }

    /**
     * Deletes a notification owned by the user.
     *
     * @param userId user identifier
     * @param notificationId notification identifier
     * @return deletion result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteNotification(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "通知参数不完整");
        }

        int deleted = notificationMapper.delete(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
        );
        if (deleted == 0) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "通知不存在");
        }

        stringRedisTemplate.delete(unreadCacheKey(userId));
        return Result.success();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String unreadCacheKey(Long userId) {
        return NotificationConstants.UNREAD_CACHE_PREFIX + userId;
    }
}

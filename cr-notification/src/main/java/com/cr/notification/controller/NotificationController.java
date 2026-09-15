package com.cr.notification.controller;

import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification API.
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Gets a paginated notification list.
     *
     * @param userId user identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return notification page
     */
    @GetMapping("/list")
    public Result getNotificationList(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (userId == null) {
            return unauthorized();
        }
        return notificationService.getNotificationList(userId, pageNum, pageSize);
    }

    /**
     * Gets the unread notification count.
     *
     * @param userId user identifier
     * @return unread count
     */
    @GetMapping("/unread-count")
    public Result getUnreadCount(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return notificationService.getUnreadCount(userId);
    }

    /**
     * Marks a notification as read.
     *
     * @param id notification identifier
     * @param userId user identifier
     * @return update result
     */
    @PutMapping("/read/{id}")
    public Result markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return notificationService.markAsRead(userId, id);
    }

    /**
     * Marks all notifications as read.
     *
     * @param userId user identifier
     * @return update result
     */
    @PutMapping("/read-all")
    public Result markAllAsRead(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return notificationService.markAllAsRead(userId);
    }

    /**
     * Deletes a notification.
     *
     * @param id notification identifier
     * @param userId user identifier
     * @return deletion result
     */
    @DeleteMapping("/{id}")
    public Result deleteNotification(
            @PathVariable Long id,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return notificationService.deleteNotification(userId, id);
    }

    private Result unauthorized() {
        return Result.fail(
                ResultCode.UNAUTHORIZED.getCode(),
                ResultCode.UNAUTHORIZED.getMsg()
        );
    }
}

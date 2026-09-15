package com.cr.notification.service;

import com.cr.common.result.Result;

/**
 * Notification service.
 */
public interface NotificationService {

    /**
     * Gets a paginated notification list.
     *
     * @param userId user identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return notification page
     */
    Result getNotificationList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * Gets the unread notification count.
     *
     * @param userId user identifier
     * @return unread count
     */
    Result getUnreadCount(Long userId);

    /**
     * Marks a notification as read.
     *
     * @param userId user identifier
     * @param notificationId notification identifier
     * @return update result
     */
    Result markAsRead(Long userId, Long notificationId);

    /**
     * Marks all notifications as read.
     *
     * @param userId user identifier
     * @return update result
     */
    Result markAllAsRead(Long userId);

    /**
     * Deletes a notification.
     *
     * @param userId user identifier
     * @param notificationId notification identifier
     * @return deletion result
     */
    Result deleteNotification(Long userId, Long notificationId);
}

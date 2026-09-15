package com.cr.image.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.image.entity.Image;
import com.cr.image.entity.ImageComment;
import com.cr.image.mapper.ImageCommentMapper;
import com.cr.image.mapper.ImageMapper;
import com.cr.image.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default image comment service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String CONTENT_EXCHANGE = "content.exchange";

    private static final String NOTIFICATION_COMMENT_ROUTING_KEY = "notification.comment";

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ImageCommentMapper imageCommentMapper;

    private final ImageMapper imageMapper;

    private final RabbitTemplate rabbitTemplate;

    /**
     * Adds a comment and increments the image comment count.
     *
     * @param userId user identifier
     * @param username display username
     * @param imageId image identifier
     * @param content comment content
     * @param parentId parent comment identifier
     * @return new comment
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ImageComment> addComment(
            Long userId,
            String username,
            Long imageId,
            String content,
            Long parentId) {
        if (userId == null || imageId == null || StrUtil.isBlank(content)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "评论参数不完整");
        }
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "图片不存在");
        }

        ImageComment comment = new ImageComment();
        comment.setImageId(imageId);
        comment.setUserId(userId);
        comment.setUsername(StrUtil.isBlank(username) ? "用户" + userId : username);
        comment.setContent(content);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setCreateTime(LocalDateTime.now());
        if (imageCommentMapper.insert(comment) != 1) {
            return Result.fail("添加评论失败");
        }

        sendCommentMessage(userId, imageId, comment.getId(), image.getAuthorId());
        return Result.success(comment);
    }

    /**
     * Deletes a comment owned by the supplied user.
     *
     * @param commentId comment identifier
     * @param userId user identifier
     * @return deletion result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteComment(Long commentId, Long userId) {
        ImageComment comment = imageCommentMapper.selectById(commentId);
        if (comment == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            return Result.fail(ResultCode.FORBIDDEN.getCode(), "无权删除该评论");
        }

        imageCommentMapper.deleteById(commentId);
        return Result.success();
    }

    /**
     * Gets a paginated comment list.
     *
     * @param imageId image identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return comment page
     */
    @Override
    public Result<Page<ImageComment>> getCommentList(
            Long imageId,
            Integer pageNum,
            Integer pageSize) {
        Page<ImageComment> page = new Page<>(
                normalizePageNum(pageNum),
                normalizePageSize(pageSize)
        );
        return Result.success(imageCommentMapper.selectPage(
                page,
                new LambdaQueryWrapper<ImageComment>()
                        .eq(ImageComment::getImageId, imageId)
                        .orderByDesc(ImageComment::getCreateTime)
                        .orderByDesc(ImageComment::getId)
        ));
    }

    private void sendCommentMessage(
            Long userId,
            Long imageId,
            Long commentId,
            Long authorId) {
        Map<String, Object> message = new HashMap<>();
        message.put("userId", userId);
        message.put("imageId", imageId);
        message.put("commentId", commentId);
        message.put("authorId", authorId);
        message.put("type", "comment");
        try {
            rabbitTemplate.convertAndSend(
                    CONTENT_EXCHANGE,
                    NOTIFICATION_COMMENT_ROUTING_KEY,
                    message
            );
        } catch (AmqpException exception) {
            log.warn(
                    "Failed to send comment notification for image {} and comment {}",
                    imageId,
                    commentId,
                    exception
            );
        }
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
}

package com.cr.image.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.result.Result;
import com.cr.image.entity.ImageComment;

/**
 * Image comment service.
 */
public interface CommentService {

    /**
     * Adds a comment to an image.
     *
     * @param userId user identifier
     * @param username display username
     * @param imageId image identifier
     * @param content comment content
     * @param parentId parent comment identifier
     * @return new comment
     */
    Result<ImageComment> addComment(
            Long userId,
            String username,
            Long imageId,
            String content,
            Long parentId);

    /**
     * Deletes a comment owned by the user.
     *
     * @param commentId comment identifier
     * @param userId user identifier
     * @return deletion result
     */
    Result<Void> deleteComment(Long commentId, Long userId);

    /**
     * Gets a paginated comment list.
     *
     * @param imageId image identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return comment page
     */
    Result<Page<ImageComment>> getCommentList(
            Long imageId,
            Integer pageNum,
            Integer pageSize);
}

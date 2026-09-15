package com.cr.image.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.image.entity.ImageComment;
import com.cr.image.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Image comment API.
 */
@RestController
@RequestMapping("/api/image/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Adds a comment to an image.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @param content comment content
     * @param parentId parent comment identifier
     * @return new comment
     */
    @PostMapping("/add")
    public Result<ImageComment> addComment(
            @RequestHeader(value = "userId", required = false) Long userId,
            @RequestParam Long imageId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {
        if (userId == null) {
            return unauthorized();
        }
        return commentService.addComment(userId, null, imageId, content, parentId);
    }

    /**
     * Deletes a comment.
     *
     * @param id comment identifier
     * @param userId user identifier
     * @return deletion result
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return unauthorized();
        }
        return commentService.deleteComment(id, userId);
    }

    /**
     * Gets a paginated comment list.
     *
     * @param imageId image identifier
     * @param pageNum page number
     * @param pageSize page size
     * @return comment page
     */
    @GetMapping("/list")
    public Result<Page<ImageComment>> getCommentList(
            @RequestParam Long imageId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return commentService.getCommentList(imageId, pageNum, pageSize);
    }

    private Result unauthorized() {
        return Result.fail(
                ResultCode.UNAUTHORIZED.getCode(),
                ResultCode.UNAUTHORIZED.getMsg()
        );
    }
}

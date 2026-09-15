package com.cr.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Common business status codes.
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在");

    /**
     * Business status code.
     */
    private final Integer code;

    /**
     * Status description.
     */
    private final String msg;
}

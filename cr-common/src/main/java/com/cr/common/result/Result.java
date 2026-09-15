package com.cr.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Unified API response.
 *
 * @param <T> response data type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Business status code.
     */
    private Integer code;

    /**
     * Response message.
     */
    private String msg;

    /**
     * Response data.
     */
    private T data;

    /**
     * Creates a successful response without data.
     *
     * @param <T> response data type
     * @return successful response
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * Creates a successful response with data.
     *
     * @param data response data
     * @param <T> response data type
     * @return successful response
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * Creates a failed response with the default failure code.
     *
     * @param msg failure message
     * @param <T> response data type
     * @return failed response
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * Creates a failed response.
     *
     * @param code failure code
     * @param msg failure message
     * @param <T> response data type
     * @return failed response
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}

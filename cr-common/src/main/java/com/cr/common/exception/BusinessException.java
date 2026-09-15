package com.cr.common.exception;

import com.cr.common.result.ResultCode;
import lombok.Getter;

/**
 * Exception for expected business failures.
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Business status code.
     */
    private final Integer code;

    /**
     * Creates a business exception with the default failure code.
     *
     * @param msg failure message
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * Creates a business exception with a custom code.
     *
     * @param code business status code
     * @param msg failure message
     */
    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}

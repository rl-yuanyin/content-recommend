package com.cr.recommend.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User behavior request.
 */
@Data
public class BehaviorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Image identifier.
     */
    private Long imageId;

    /**
     * Behavior type. 1 view, 2 like, 3 comment, 5 collect, 6 download.
     */
    private Integer behaviorType;

    /**
     * Dwell time in seconds.
     */
    private Integer duration = 0;
}

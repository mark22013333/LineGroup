package com.cheng.linegroup.exception;

import com.cheng.linegroup.enums.ApiResult;
import lombok.Getter;

/**
 * 資源找不到異常
 * 當請求的資源不存在時拋出此異常
 *
 * @author cheng
 * @since 2025/6/8 23:05
 **/
@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final int code;

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = ApiResult.NOT_FOUND.getCode();
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.code = ApiResult.NOT_FOUND.getCode();
    }

    public static ResourceNotFoundException create(String resourceName, Object identifier) {
        return new ResourceNotFoundException(String.format("找不到指定的 %s: %s", resourceName, identifier));
    }
}

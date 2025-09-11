package com.cheng.linegroup.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 統一 API 回應格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "統一 API 回應格式")
public class ApiResponse<T> {

    @Schema(description = "回應代碼", example = "200")
    private Integer code;

    @Schema(description = "回應訊息", example = "操作成功")
    private String message;

    @Schema(description = "回應資料")
    private T data;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "時間戳記", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "請求追蹤ID", example = "abc123def456")
    private String traceId;

    /**
     * 成功回應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功回應（自訂訊息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功回應（無資料）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功回應（無資料，自訂訊息）
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗回應
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗回應（預設 500 錯誤）
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 參數驗證錯誤
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return ApiResponse.<T>builder()
                .code(400)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 未授權錯誤
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(401)
                .message(message != null ? message : "未授權存取")
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 禁止存取錯誤
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code(403)
                .message(message != null ? message : "禁止存取")
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 資源不存在錯誤
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(404)
                .message(message != null ? message : "資源不存在")
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 業務邏輯錯誤
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return ApiResponse.<T>builder()
                .code(422)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 設定追蹤ID
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}

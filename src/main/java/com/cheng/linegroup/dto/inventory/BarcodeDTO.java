package com.cheng.linegroup.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 條碼相關 DTO
 */
public class BarcodeDTO {

    /**
     * 條碼產生請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼產生請求")
    public static class GenerateRequest {

        @NotBlank(message = "條碼內容不能為空")
        @Size(max = 200, message = "條碼內容長度不能超過200字元")
        @Schema(description = "條碼內容", example = "INV001001", required = true)
        private String content;

        @Schema(description = "條碼寬度", example = "200")
        private Integer width;

        @Schema(description = "條碼高度", example = "200")
        private Integer height;

        @Schema(description = "條碼格式", example = "QR_CODE", allowableValues = {"QR_CODE", "CODE_128"})
        private String format;
    }

    /**
     * 條碼產生回應 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼產生回應")
    public static class GenerateResponse {

        @Schema(description = "條碼內容", example = "INV001001")
        private String content;

        @Schema(description = "條碼格式", example = "QR_CODE")
        private String format;

        @Schema(description = "圖片寬度", example = "200")
        private Integer width;

        @Schema(description = "圖片高度", example = "200")
        private Integer height;

        @Schema(description = "圖片資料")
        private byte[] imageData;

        @Schema(description = "內容類型", example = "image/png")
        private String contentType;

        @Schema(description = "檔案大小", example = "1024")
        private Long fileSize;
    }

    /**
     * 條碼掃描請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼掃描請求")
    public static class ScanRequest {

        @NotBlank(message = "條碼不能為空")
        @Size(max = 200, message = "條碼長度不能超過200字元")
        @Schema(description = "條碼內容", example = "INV001001", required = true)
        private String barcode;
    }

    /**
     * 條碼掃描回應 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼掃描回應")
    public static class ScanResponse {

        @Schema(description = "條碼內容", example = "INV001001")
        private String barcode;

        @Schema(description = "是否找到對應物品", example = "true")
        private Boolean found;

        @Schema(description = "匹配類型", example = "BARCODE", allowableValues = {"BARCODE", "ITEM_CODE"})
        private String matchType;

        @Schema(description = "物品ID", example = "1")
        private Long itemId;

        @Schema(description = "物品代碼", example = "COMP001")
        private String itemCode;

        @Schema(description = "物品名稱", example = "Dell OptiPlex 7090 桌上型電腦")
        private String itemName;

        @Schema(description = "分類名稱", example = "電腦設備")
        private String categoryName;

        @Schema(description = "當前庫存", example = "5")
        private Integer currentStock;

        @Schema(description = "可用庫存", example = "3")
        private Integer availableStock;

        @Schema(description = "存放位置", example = "IT室-A01")
        private String location;

        @Schema(description = "物品狀態", example = "ACTIVE")
        private String status;

        @Schema(description = "回應訊息", example = "找到對應物品")
        private String message;
    }

    /**
     * 批次產生條碼請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批次產生條碼請求")
    public static class BatchGenerateRequest {

        @NotEmpty(message = "物品ID列表不能為空")
        @Schema(description = "物品ID列表", required = true)
        private List<Long> itemIds;

        @Schema(description = "條碼格式", example = "QR_CODE", allowableValues = {"QR_CODE", "CODE_128"})
        private String format;

        @Schema(description = "是否覆蓋現有條碼", example = "false")
        private Boolean overwrite;
    }

    /**
     * 批次產生條碼回應 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批次產生條碼回應")
    public static class BatchGenerateResponse {

        @Schema(description = "總數量", example = "10")
        private Integer totalCount;

        @Schema(description = "成功數量", example = "8")
        private Integer successCount;

        @Schema(description = "失敗數量", example = "2")
        private Integer failCount;

        @Schema(description = "錯誤訊息", example = "物品ID 999 不存在")
        private String errorMessages;
    }

    /**
     * 條碼驗證請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼驗證請求")
    public static class ValidationRequest {

        @NotBlank(message = "條碼不能為空")
        @Size(max = 200, message = "條碼長度不能超過200字元")
        @Schema(description = "條碼內容", example = "INV001001", required = true)
        private String barcode;

        @Schema(description = "排除的物品ID（用於編輯時驗證）", example = "1")
        private Long excludeItemId;
    }

    /**
     * 條碼驗證回應 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼驗證回應")
    public static class ValidationResponse {

        @Schema(description = "條碼內容", example = "INV001001")
        private String barcode;

        @Schema(description = "格式是否有效", example = "true")
        private Boolean isValid;

        @Schema(description = "條碼是否已存在", example = "false")
        private Boolean exists;

        @Schema(description = "驗證訊息", example = "條碼格式正確")
        private String message;
    }
}

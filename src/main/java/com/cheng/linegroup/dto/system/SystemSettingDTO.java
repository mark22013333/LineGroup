package com.cheng.linegroup.dto.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系統設置資料傳輸物件
 *
 * @author cheng
 * @since 2025/6/8 22:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系統設置資料傳輸物件")
public class SystemSettingDTO {
    
    @Schema(description = "設置 ID")
    private Long id;
    
    @NotBlank(message = "設置鍵不得為空")
    @Size(max = 100, message = "設置鍵長度不得超過100個字元")
    @Schema(description = "設置鍵", required = true)
    private String key;
    
    @NotBlank(message = "設置值不得為空")
    @Size(max = 1000, message = "設置值長度不得超過1000個字元")
    @Schema(description = "設置值", required = true)
    private String value;
    
    @Size(max = 500, message = "設置描述長度不得超過500個字元")
    @Schema(description = "設置描述")
    private String description;
    
    @Size(max = 50, message = "設置類別長度不得超過50個字元")
    @Schema(description = "設置類別")
    private String category;
    
    @Schema(description = "是否公開")
    private Boolean isPublic;
    
    @Schema(description = "建立時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Schema(description = "建立者")
    private String createdBy;
    
    @Schema(description = "更新者")
    private String updatedBy;
}

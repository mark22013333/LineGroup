package com.cheng.linegroup.dto.common;

import com.cheng.linegroup.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分頁回應包裝類
 * 
 * @author cheng
 * @since 2025/5/3 11:40
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分頁回應包裝類")
public class PageResponse<T> extends BaseDto {
    
    @Schema(description = "總筆數")
    private long total;
    
    @Schema(description = "總頁數")
    private int totalPages;
    
    @Schema(description = "當前頁碼")
    private int page;
    
    @Schema(description = "每頁筆數")
    private int size;
    
    @Schema(description = "是否有下一頁")
    private boolean hasNext;
    
    @Schema(description = "是否有上一頁")
    private boolean hasPrevious;
    
    @Schema(description = "資料內容")
    private List<T> content;
    
    /**
     * 建立分頁回應
     * 
     * @param content 資料內容
     * @param total 總筆數
     * @param page 當前頁碼
     * @param size 每頁筆數
     * @return 分頁回應
     */
    public static <T> PageResponse<T> of(List<T> content, long total, int page, int size) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        
        // 計算總頁數
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / size);
        response.setTotalPages(totalPages);
        
        // 檢查是否有上一頁和下一頁
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);
        
        return response;
    }
}

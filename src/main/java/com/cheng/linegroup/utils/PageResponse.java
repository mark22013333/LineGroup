package com.cheng.linegroup.utils;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分頁回應物件
 * 
 * @author cheng
 * @since 2025/5/15
 * @param <T> 內容資料類型
 */
@Data
public class PageResponse<T> {
    /**
     * 當前頁碼（從0開始）
     */
    private int page;
    
    /**
     * 每頁資料筆數
     */
    private int size;
    
    /**
     * 總筆數
     */
    private long total;
    
    /**
     * 總頁數
     */
    private int totalPages;
    
    /**
     * 是否為第一頁
     */
    private boolean first;
    
    /**
     * 是否為最後一頁
     */
    private boolean last;
    
    /**
     * 頁面內容
     */
    private List<T> content;
    
    /**
     * 從Spring Data的Page物件建立PageResponse物件
     *
     * @param page Spring Data分頁物件
     * @param <T> 內容資料類型
     * @return 自定義分頁回應物件
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotal(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setContent(page.getContent());
        return response;
    }
    
    /**
     * 從內容列表和分頁資訊建立PageResponse物件
     *
     * @param content 內容列表
     * @param page 頁碼
     * @param size 每頁筆數
     * @param total 總筆數
     * @param <T> 內容資料類型
     * @return 自定義分頁回應物件
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        PageResponse<T> response = new PageResponse<>();
        response.setPage(page);
        response.setSize(size);
        response.setTotal(total);
        response.setTotalPages((int) Math.ceil((double) total / size));
        response.setFirst(page == 0);
        response.setLast(page >= response.getTotalPages() - 1);
        response.setContent(content);
        return response;
    }
}

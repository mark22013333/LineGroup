package com.cheng.linegroup.service.common;

import com.cheng.linegroup.dto.common.BaseQueryParams;
import com.cheng.linegroup.dto.common.PageResponse;

/**
 * 通用查詢服務介面
 * 提供所有列表型功能的基礎查詢能力
 * 
 * @author cheng
 * @since 2025/5/3 11:55
 */
public interface BaseQueryService<T, Q extends BaseQueryParams> {
    
    /**
     * 分頁查詢
     * 
     * @param queryParams 查詢參數
     * @return 分頁結果
     */
    PageResponse<T> findByPage(Q queryParams);
}

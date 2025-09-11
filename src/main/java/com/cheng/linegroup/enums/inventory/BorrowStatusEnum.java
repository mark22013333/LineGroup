package com.cheng.linegroup.enums.inventory;

import lombok.Getter;

/**
 * 借還狀態枚舉
 */
@Getter
public enum BorrowStatusEnum {
    
    /**
     * 已借出
     */
    BORROWED("已借出", "物品已成功借出"),
    
    /**
     * 已歸還
     */
    RETURNED("已歸還", "物品已完全歸還"),
    
    /**
     * 逾期
     */
    OVERDUE("逾期", "物品歸還逾期"),
    
    /**
     * 部分歸還
     */
    PARTIAL_RETURNED("部分歸還", "物品已部分歸還");

    private final String displayName;
    private final String description;

    BorrowStatusEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

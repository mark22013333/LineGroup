package com.cheng.linegroup.enums.inventory;

import lombok.Getter;

/**
 * 庫存異動參考類型枚舉
 */
@Getter
public enum ReferenceTypeEnum {
    
    /**
     * 採購
     */
    PURCHASE("採購", "採購入庫"),
    
    /**
     * 借出
     */
    BORROW("借出", "物品借出"),
    
    /**
     * 歸還
     */
    RETURN("歸還", "物品歸還"),
    
    /**
     * 調整
     */
    ADJUSTMENT("調整", "庫存調整"),
    
    /**
     * 損壞
     */
    DAMAGE("損壞", "物品損壞"),
    
    /**
     * 遺失
     */
    LOSS("遺失", "物品遺失");

    private final String displayName;
    private final String description;

    ReferenceTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

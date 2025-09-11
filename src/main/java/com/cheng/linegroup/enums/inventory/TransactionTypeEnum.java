package com.cheng.linegroup.enums.inventory;

import lombok.Getter;

/**
 * 庫存異動類型枚舉
 */
@Getter
public enum TransactionTypeEnum {
    
    /**
     * 入庫
     */
    IN("入庫", "增加庫存"),
    
    /**
     * 出庫
     */
    OUT("出庫", "減少庫存"),
    
    /**
     * 調整
     */
    ADJUST("調整", "庫存調整"),
    
    /**
     * 損壞
     */
    DAMAGED("損壞", "物品損壞"),
    
    /**
     * 遺失
     */
    LOST("遺失", "物品遺失");

    private final String displayName;
    private final String description;

    TransactionTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

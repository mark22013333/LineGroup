package com.cheng.linegroup.enums.inventory;

import lombok.Getter;

/**
 * 歸還狀態枚舉
 */
@Getter
public enum ReturnConditionEnum {
    
    /**
     * 良好
     */
    GOOD("良好", "物品狀態良好"),
    
    /**
     * 損壞
     */
    DAMAGED("損壞", "物品有損壞"),
    
    /**
     * 遺失
     */
    LOST("遺失", "物品遺失");

    private final String displayName;
    private final String description;

    ReturnConditionEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}

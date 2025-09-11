package com.cheng.linegroup.enums;

/**
 * 狀態枚舉
 */
public enum StatusEnum {
    /**
     * 啟用
     */
    ACTIVE(1, "啟用"),
    
    /**
     * 停用
     */
    INACTIVE(0, "停用"),
    
    /**
     * 刪除
     */
    DELETED(-1, "刪除");

    private final int code;
    private final String description;

    StatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根據代碼取得枚舉
     */
    public static StatusEnum fromCode(int code) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的狀態代碼: " + code);
    }
}

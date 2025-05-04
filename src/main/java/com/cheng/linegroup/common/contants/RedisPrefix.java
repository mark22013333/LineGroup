package com.cheng.linegroup.common.contants;

import lombok.experimental.UtilityClass;

/**
 * @author cheng
 * @since 2024/3/8 22:41
 **/
@UtilityClass
public class RedisPrefix {

    /**
     * Token 黑名單
     */
    public static final String BLACKLIST_TOKEN = "token:blacklist:";
    
    /**
     * 活躍令牌資訊
     */
    public static final String ACTIVE_TOKEN = "token:active:";
    
    /**
     * 重整令牌資訊
     */
    public static final String REFRESH_TOKEN = "token:refresh:";

    public static final String PERSONAL = "personal";
}

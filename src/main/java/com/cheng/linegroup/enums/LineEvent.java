package com.cheng.linegroup.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author cheng
 * @since 2024/2/11 00:23
 **/
public enum LineEvent {
    /**
     * LINE-BOT事件名稱: 加入好友
     */
    FOLLOW("follow"),

    /**
     * LINE-BOT事件名稱: 封鎖好友
     */
    UNFOLLOW("unfollow"),

    /**
     * LINE-BOT事件名稱: BOT加入群組
     * 成員建立群組時有邀請BOT，則會同時觸發 {@link #JOIN} 和 {@link #MEMBER_JOINED} 事件
     */
    JOIN("join"),

    /**
     * LINE-BOT事件名稱: BOT離開群組
     * 成員將BOT從群組中移除時會觸發此事件
     */
    LEAVE("leave"),

    /**
     * LINE-BOT事件名稱: 成員離開群組
     */
    MEMBER_LEFT("memberLeft"),

    /**
     * LINE-BOT事件名稱: 成員加入群組
     */
    MEMBER_JOINED("memberJoined"),

    /**
     * LINE-BOT事件名稱: BOT收到訊息
     */
    MESSAGE("message"),

    /**
     * LINE-BOT事件名稱: BOT回傳的訊息
     * BOT傳送訊息給使用者時，可在訊息中的按鈕設定有回傳資料的類型
     * 當使用者點擊按鈕時，會觸發此事件，並回傳資料
     */
    POSTBACK("postback"),

    NONE("none");

    private final String eventName;

    LineEvent(String eventName) {
        this.eventName = eventName;
    }

    private static final Map<String, LineEvent> NAME_TO_EVENT_MAP;

    static {
        NAME_TO_EVENT_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(e -> e.eventName, Function.identity()));
    }

    public static LineEvent getEvent(String eventName) {
        return NAME_TO_EVENT_MAP.getOrDefault(eventName, NONE);
    }

}

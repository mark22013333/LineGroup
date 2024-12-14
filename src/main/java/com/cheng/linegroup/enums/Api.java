package com.cheng.linegroup.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cheng
 * @since 2023/12/3 2:04 PM
 **/
@Getter
@AllArgsConstructor
public enum Api implements Uri {

    /**
     * 示警訊息查詢
     */
    DATASET("api/dataset"),

    /**
     * 查詢即時示警訊息清單
     */
    DATA_STORE("api/datastore"),

    /**
     * 查詢示警訊息內容
     */
    DUMP_DATA_STORE("api/dump/datastore"),

    LINE_NOTIFY_PUSH("/api/notify"),

    /**
     * OAuth2 authorization endpoint URI
     */
    LINE_NOTIFY_OAUTH("/oauth/authorize"),

    LINE_NOTIFY_TOKEN("/oauth/token"),

    LINE_NOTIFY_CALLBACK("/notify/authCode"),

    LINE_MESSAGE_PUSH("/bot/message/push"),

    LINE_MESSAGE_REPLY("/bot/message/reply"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#get-profile">
     * Get profile
     * </a>
     * <p>
     * param-1: line user id
     */
    LINE_GET_USER_PROFILE("/bot/profile/%s"),

    /**
     * Get group chat summary
     * param-1: groupId
     */
    LINE_GROUP_SUMMARY("bot/group/%s/summary"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#get-group-member-profile">
     * Get group chat member profile
     * </a>
     * <p>
     * param-1: groupId<br>
     * param-2: userId
     */
    LINE_GROUP_MEMBER_PROFILE("bot/group/%s/member/%s"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#get-group-member-user-ids">
     * Get group chat member user IDs
     * </a>
     * <p>
     * param-1: groupId<br>
     * param-2: nextToken<br>
     * (A continuation token to get the next array of user IDs of the members in the group chat. Returned only when there are remaining user IDs that were not returned in memberIds in the original request.)
     */
    LINE_GROUP_MEMBER_IDS("bot/group/%s/members/ids"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#get-content">
     * Get message content
     * </a>
     * param-1: messageId
     */
    LINE_MESSAGE_CONTENT("bot/message/%s/content"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#verify-video-or-audio-preparation-status">
     * Get the content preparation status of video or audio
     * </a>
     * param-1: messageId
     */
    LINE_MESSAGE_CONTENT_TRANSCODING("bot/message/%s/content/transcoding"),

    /**
     * <a href="https://developers.line.biz/en/reference/messaging-api/#get-image-or-video-preview">
     * Get the content preview of video or image
     * </a>
     * param-1: messageId
     */
    LINE_MESSAGE_CONTENT_PREVIEW("bot/message/%s/content/preview"),
    ;

    private final String uri;

}

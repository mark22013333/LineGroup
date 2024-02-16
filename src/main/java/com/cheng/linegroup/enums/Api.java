package com.cheng.linegroup.enums;

import lombok.Getter;

/**
 * @author cheng
 * @since 2023/12/3 2:04 PM
 **/
@Getter
public enum Api implements Uri {

    LINE_NOTIFY_PUSH("/api/notify"),

    /**
     * OAuth2 authorization endpoint URI
     */
    LINE_NOTIFY_OAUTH("/oauth/authorize"),

    LINE_NOTIFY_TOKEN("/oauth/token"),

    LINE_NOTIFY_CALLBACK("/notify/authCode"),

    LINE_MESSAGE_PUSH("/bot/message/push"),

    LINE_MESSAGE_REPLY("/bot/message/reply"),

    LINE_GET_USER_PROFILE("/profile"),

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
    ;

    private final String uri;

    Api(String uri) {
        this.uri = uri;
    }
}

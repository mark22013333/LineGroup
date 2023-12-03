package com.cheng.linegroup.enums;

import lombok.Getter;

/**
 * @author cheng
 * @since 2023/12/3 2:04 PM
 **/
@Getter
public enum Api implements Uri {

    LINE_NOTIFY_PUSH("/api/notify"),

    LINE_MESSAGE_PUSH("/bot/message/push"),

    /**
     * Get group chat summary
     * param-1: groupId
     */
    LINE_GROUP_SUMMARY("bot/group/%s/summary"),

    /**
     * Get group chat member profile
     * param-1: groupId
     * param-2: userId
     */
    LINE_GROUP_MEMBER_PROFILE("bot/group/%s/member/%s"),
    ;

    private final String uri;

    Api(String uri) {
        this.uri = uri;
    }
}

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

    ;

    private final String uri;

    Api(String uri) {
        this.uri = uri;
    }
}

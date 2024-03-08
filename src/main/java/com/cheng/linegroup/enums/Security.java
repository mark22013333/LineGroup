package com.cheng.linegroup.enums;

import lombok.Getter;

/**
 * @author cheng
 * @since 2024/3/7 18:54
 **/
@Getter
public enum Security implements Uri{
    LOGIN("/api/v1/auth/login"),

    ;

    private final String uri;

    Security(String uri) {
        this.uri = uri;
    }
}

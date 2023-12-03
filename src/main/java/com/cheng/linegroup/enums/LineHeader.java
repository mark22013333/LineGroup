package com.cheng.linegroup.enums;

import lombok.Getter;

/**
 * @author cheng
 * @since 2023/11/27 9:59 AM
 **/
@Getter
public enum LineHeader {

    LINE_REQUEST_ID("x-line-request-id"),

    ;

    private final String name;

    LineHeader(String name){
        this.name = name;
    }
}

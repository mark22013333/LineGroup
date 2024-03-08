package com.cheng.linegroup.enums.common;

import com.cheng.linegroup.enums.Base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author cheng
 * @since 2024/2/14 00:04
 **/
@Getter
@RequiredArgsConstructor
public enum Gender implements Base<Integer> {
    NONE(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女"),

    ;

    private final Integer value;

    private final String label;
}

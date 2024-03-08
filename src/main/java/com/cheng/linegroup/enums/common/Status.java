package com.cheng.linegroup.enums.common;

import com.cheng.linegroup.enums.Base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author cheng
 * @since 2024/3/7 22:58
 **/
@Getter
@RequiredArgsConstructor
public enum Status implements Base<Integer> {

    DISABLE(0, "停用"),
    ENABLE(1, "啟用"),

    ;

    private final Integer value;
    private final String label;

}

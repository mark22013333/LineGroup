package com.cheng.linegroup.enums;

import lombok.Getter;

/**
 * @author Cheng
 * @since 2024/11/2 16:07
 **/
@Getter
public enum BehaviorKeyword {

    AI_CHAT("."),
    BOT_LEARNING("欸唐董看到", "唐董", "唐懂", "唐墥", "唐"),
    RANDOM_IMAGE("抽"),

    ;

    private final String[] keywords;

    BehaviorKeyword(String... keywords) {
        this.keywords = keywords;
    }

}

package com.cheng.linegroup.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cheng
 * @since 2024/12/1 10:15
 **/
@Getter
@AllArgsConstructor
public enum SubscribeType {
    AQI("空氣品質", "airQuality"),
    POWER_OUTAGE("電力中斷", "electric"),
    WATER_OUTAGE("停水", "TWC_water"),

    UNKNOWN("未知"),

    SUBSCRIPTION,
    UNSUBSCRIPTION,
    ;

    SubscribeType() {
        this.capCode = null;
        this.desc = null;
    }

    SubscribeType(String capCode) {
        this.capCode = capCode;
        this.desc = null;
    }

    private final String desc;
    /**
     * 目前寫的都是預設，實際還是要看 API 返回的結果
     */
    private final String capCode;

    public static SubscribeType of(String name) {
        return Arrays.stream(values())
                .filter(e -> e.name().equals(name))
                .findFirst().orElse(UNKNOWN);
    }

    public static SubscribeType fromCapCode(String capCode) {
        return Arrays.stream(values())
                .filter(e -> capCode.equalsIgnoreCase(e.capCode))
                .findFirst().orElse(UNKNOWN);
    }

    public static List<String> getAlertList() {
        return Arrays.stream(values())
                .filter(e -> e.desc != null && !"未知".equals(e.desc))
                .map(SubscribeType::getDesc)
                .toList();
    }

}

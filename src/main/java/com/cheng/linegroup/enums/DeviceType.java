package com.cheng.linegroup.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 設備類型
 *
 * @author cheng
 * @since 2025/05/04 22:00
 */
@Getter
@RequiredArgsConstructor
public enum DeviceType {

    MOBILE(Arrays.asList(
            Pattern.compile("(?i)mobile"),
            Pattern.compile("(?i)android(?!.*tablet)"),
            Pattern.compile("(?i)iphone"),
            Pattern.compile("(?i)windows phone")
    )),

    TABLET(Arrays.asList(
            Pattern.compile("(?i)ipad"),
            Pattern.compile("(?i)android.*tablet"),
            Pattern.compile("(?i)tablet"),
            Pattern.compile("(?i)kindle")
    )),

    DESKTOP(Arrays.asList(
            Pattern.compile("(?i)windows(?!.*phone)"),
            Pattern.compile("(?i)macintosh"),
            Pattern.compile("(?i)linux")
    ));

    private final List<Pattern> patterns;

    /**
     * 檢查 User-Agent 是否匹配當前設備類型
     *
     * @param userAgent User-Agent 字串
     * @return 是否匹配
     */
    public boolean matches(String userAgent) {
        if (userAgent == null) {
            return false;
        }

        return patterns.stream()
                .anyMatch(pattern -> pattern.matcher(userAgent).find());
    }
}

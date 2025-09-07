package com.cheng.linegroup.utils;

import com.cheng.linegroup.enums.DeviceType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

/**
 * Web相關工具類
 *
 * @author cheng
 * @since 2025/05/04 22:03
 */
@Slf4j
@UtilityClass
public class WebUtils {

    /**
     * 取得客戶端真實IP地址
     * 處理各種代理和負載均衡場景
     *
     * @param request HTTP請求
     * @return 客戶端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 取第一個IP（多級代理的情況下）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 檢查請求是否來自移動設備
     *
     * @param request HTTP請求
     * @return 是否為移動設備
     */
    public static boolean isMobileDevice(HttpServletRequest request) {
        return getDeviceType(request) == DeviceType.MOBILE;
    }

    /**
     * 檢查請求是否來自平板設備
     *
     * @param request HTTP請求
     * @return 是否為平板設備
     */
    public static boolean isTabletDevice(HttpServletRequest request) {
        return getDeviceType(request) == DeviceType.TABLET;
    }

    /**
     * 檢查請求是否來自桌面設備
     *
     * @param request HTTP請求
     * @return 是否為桌面設備
     */
    public static boolean isDesktopDevice(HttpServletRequest request) {
        return getDeviceType(request) == DeviceType.DESKTOP;
    }

    /**
     * 取得請求來源的設備類型
     *
     * @param request HTTP請求
     * @return 設備類型枚舉
     */
    public static DeviceType getDeviceType(HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if (StringUtils.isBlank(userAgent)) {
            return DeviceType.DESKTOP; // 預設為桌面設備
        }

        if (DeviceType.MOBILE.matches(userAgent)) {
            return DeviceType.MOBILE;
        } else if (DeviceType.TABLET.matches(userAgent)) {
            return DeviceType.TABLET;
        } else {
            return DeviceType.DESKTOP;
        }
    }

    /**
     * 取得請求的完整URL (包含查詢參數)
     *
     * @param request HTTP請求
     * @return 完整URL
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        url.append(scheme).append("://").append(serverName);

        // 只有非預設埠才需要顯示
        if (!(scheme.equals("http") && serverPort == 80) &&
                !(scheme.equals("https") && serverPort == 443)) {
            url.append(":").append(serverPort);
        }

        url.append(requestURI);

        if (StringUtils.isNotBlank(queryString)) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }
}

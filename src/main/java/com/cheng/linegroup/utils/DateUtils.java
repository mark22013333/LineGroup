package com.cheng.linegroup.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author cheng
 * @since 2024/3/7 23:59
 **/
@Slf4j
public class DateUtils {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String HH_MM_SS = "HH:mm:ss";

    /**
     * 取得今天日期
     *
     * @return {@link Date}
     */
    public static Date getCurrDate() {
        return localDate2Date(LocalDate.now());
    }

    /**
     * 取得現在時間
     *
     * @return {@link Date}
     */
    public static Date getCurrDateTime() {
        return localDateTime2Date(LocalDateTime.now());
    }

    /**
     * 取得今天日期
     *
     * @return {@link String}
     */
    public static String getCurrDateStr() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    /**
     * 取得現在時間
     *
     * @return {@link String}
     */
    public static String getCurrDateTimeStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }

    /**
     * 取時間最大值
     *
     * @return {@link String}
     */
    public static String getTimeMaxStr() {
        return LocalTime.MAX.format(DateTimeFormatter.ofPattern(HH_MM_SS));
    }

    /**
     * 取時間最小值
     *
     * @return {@link String}
     */
    public static String getTimeMinStr() {
        return LocalTime.MIN.format(DateTimeFormatter.ofPattern(HH_MM_SS));
    }

    /**
     * Date轉LocalDate
     *
     * @param date {@link Date}
     */
    public static LocalDate date2LocalDate(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date轉LocalDateTime
     *
     * @param date {@link Date}
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        if (null == date) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDate轉Date
     *
     * @param localDate {@link LocalDate}
     * @return {@link Date}
     */
    public static Date localDate2Date(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * LocalDate轉Date
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Date}
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 字串轉日期
     *
     * @param strDate 日期字串
     * @param pattern 日期格式
     * @return {@link Date}
     */
    public static Date stringToDate(String strDate, String pattern) {
        if (StringUtils.isEmpty(strDate)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(strDate);
        } catch (ParseException e) {
            log.error("ERR:{}", e.getMessage(), e);
            return null;
        }
    }

    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = YYYY_MM_DD_HH_MM_SS;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.parseLong(seconds) * 1000));
    }

    public static void main(String[] args) {
        System.out.println(getCurrDate());
        System.out.println(getCurrDateTime());
        System.out.println(getCurrDateStr());
        System.out.println(getCurrDateTimeStr());
        System.out.println(getTimeMaxStr());
        System.out.println(getTimeMinStr());
    }

}

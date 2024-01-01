package com.cheng.linegroup.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author cheng
 * @since 2023/12/3 1:13 AM
 **/
@Slf4j
public class JacksonUtils {

    private JacksonUtils() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    /**
     * 序列化
     */
    public static <T> String encodeToJson(T obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("json encode error, obj={}, ERR={}", obj, ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * 反序列化
     */
    public static <T> T decodeFromJson(String json, Class<T> valueType) {
        if (!StringUtils.isBlank(json) && !Objects.isNull(valueType)) {
            try {
                return MAPPER.readValue(json, valueType);
            } catch (Exception e) {
                log.error(
                        "json decode fail,jsonString={}, type={}, ERR={}",
                        json,
                        valueType.getName(),
                        ExceptionUtils.getStackTrace(e));
            }
        }
        return null;
    }

    /**
     * 將任何 Java 物件轉換為 Jackson 的 JsonNode。
     * 此方法使用 ObjectMapper 的 valueToTree 方法來將物件轉換成 JsonNode。
     *
     * @param <T> 輸入物件的類型。
     * @param obj 要轉換成 JsonNode 的物件。可以是任何自訂或內建類型。
     * @return 轉換後的 JsonNode 。
     */
    public static <T> JsonNode toNode(T obj) {
        return MAPPER.valueToTree(obj);
    }

    /**
     * 將 JSON 陣列字串轉換成指定類型的物件列表。
     *
     * @param jsonArray JSON 陣列字串
     * @param clazz     DTO 物件的類型
     * @return 轉換後的 DTO 物件列表
     * @throws IOException 當 JSON 處理出錯時拋出異常
     */
    public static <T> List<T> convertJsonArrayToList(String jsonArray, Class<T> clazz) throws IOException {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
        return MAPPER.readValue(jsonArray, type);
    }

    /**
     * 反序列化成ObjectNode
     */
    public static ObjectNode decodeObject(String json) throws IOException {
        return (ObjectNode) MAPPER.readTree(json);
    }

    /**
     * 反序列化成list
     */
    public static <T> List<T> decode2List(String json, Class<T> clazz) {
        if (!StringUtils.isBlank(json) && !Objects.isNull(clazz)) {
            try {
                return MAPPER.readValue(
                        json, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            } catch (Exception e) {
                log.error("json decode2list fail, json={}, classType={}, ERR={}",
                        json,
                        clazz.getName(),
                        ExceptionUtils.getStackTrace(e));
            }
        }
        log.warn("####=> decode FAIL. return null");
        return null;
    }


    /**
     * map轉class物件
     */
    public static <T> T mapToObject(Map<String, String> map, Class<T> clazz) {
        String json = null;
        try {
            json = MAPPER.writeValueAsString(map);
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("json map2object fail, map={}, ClassName={}, ERR={}",
                    json,
                    clazz.getName(),
                    ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static Map<String, String> toMap(String json) {
        return decodeFromJson(json, Map.class);
    }

    public static ObjectNode genJsonObject() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode genJsonArray() {
        return MAPPER.createArrayNode();
    }

    public static String toJsonString(Object content) {
        try {
            return MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return content.getClass().getName() + '@' + Integer.toHexString(content.hashCode());
        }
    }

    public static ObjectNode toObjectNode(String jsonStr) throws IOException {
        return (ObjectNode) MAPPER.readTree(jsonStr);
    }

    public static ArrayNode toArrayNode(String jsonStr) throws IOException {
        return (ArrayNode) MAPPER.readTree(jsonStr);
    }

    public static JsonNode toJsonNode(String jsonStr) throws IOException {
        return MAPPER.readTree(jsonStr);
    }

    public static boolean hasEscape(String text) {
        return text.contains(StringUtils.LF)
                || text.contains(StringUtils.CR)
                || text.contains("\\")
                || text.contains("\t")
                || text.contains("\"");
    }
}


package com.cheng.linegroup.utils;

import com.cheng.linegroup.utils.dto.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkHttpUtils 工具類測試
 * <p>
 * 使用 TestOkHttpUtils 測試類進行測試，繞過對 Spring 上下文的依賴
 *
 * @author cheng
 * @since 2025/06/07
 */
public class OkHttpUtilsTest {

    // 使用 JSONPlaceholder 免費測試 API
    private static final String TEST_API_BASE_URL = "https://jsonplaceholder.typicode.com";

    /**
     * 測試開始前設定
     */
    @BeforeAll
    public static void setUp() {
        System.out.println("====== 開始執行 OkHttpUtils 功能測試 ======");
    }

    @Test
    @DisplayName("測試 GET 請求")
    public void testGetRequest() throws IOException {
        // 建立請求 URL - 取得特定 ID 的 post 資料
        String url = TEST_API_BASE_URL + "/posts/1";

        // 執行 GET 請求並同步取得回應
        ApiResponse response = TestOkHttpUtils.builder()
                .addBrowserHeader() // 添加瀏覽器標頭
                .get(url)
                .sync();

        // 驗證回應
        assertNotNull(response, "回應不應為空");
        assertEquals(200, response.getHttpStatusCode(), "HTTP 狀態碼應為 200");

        // 驗證回應內容包含預期的資料結構
        String responseData = response.getResultData();
        assertNotNull(responseData, "回應資料不應為空");
        System.out.println("GET 請求回應: " + responseData);

        // 解析 JSON 回應
        JsonNode jsonResponse = JacksonUtils.toJsonNode(responseData);
        assertNotNull(jsonResponse.get("id"), "回應應包含 id 欄位");
        assertNotNull(jsonResponse.get("title"), "回應應包含 title 欄位");
        assertNotNull(jsonResponse.get("body"), "回應應包含 body 欄位");
    }

    @Test
    @DisplayName("測試 GET 請求帶查詢參數")
    public void testGetRequestWithQueryParams() throws IOException {
        // 建立請求 URL - 查詢 posts 資料
        String url = TEST_API_BASE_URL + "/posts";

        // 添加查詢參數
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("userId", "1");

        // 執行 GET 請求並同步取得回應
        ApiResponse response = TestOkHttpUtils.builder()
                .addBrowserHeader()
                .addParam(queryParams) // 添加查詢參數
                .get(url)
                .sync();

        // 驗證回應
        assertNotNull(response, "回應不應為空");
        assertEquals(200, response.getHttpStatusCode(), "HTTP 狀態碼應為 200");

        // 驗證回應內容包含預期的資料
        String responseData = response.getResultData();
        assertNotNull(responseData, "回應資料不應為空");
        System.out.println("GET 請求帶參數回應的前 100 個字元: " +
                (responseData.length() > 100 ? responseData.substring(0, 100) + "..." : responseData));

        // 解析 JSON 回應 (預期是一個陣列)
        JsonNode jsonResponse = JacksonUtils.toJsonNode(responseData);
        assertTrue(jsonResponse.isArray(), "回應應為陣列");
        assertFalse(jsonResponse.isEmpty(), "回應陣列不應為空");
    }

    @Test
    @DisplayName("測試 POST 請求 (JSON)")
    public void testPostRequestJson() throws IOException {
        // 建立請求 URL - 建立 post 資料
        String url = TEST_API_BASE_URL + "/posts";

        // 建立 JSON 請求主體
        ObjectNode jsonBody = JacksonUtils.genJsonObject();
        jsonBody.put("title", "測試標題");
        jsonBody.put("body", "這是測試內容文字");
        jsonBody.put("userId", 1);

        // 執行 POST 請求並同步取得回應
        ApiResponse response = TestOkHttpUtils.builder()
                .addBrowserHeader()
                .addParam(jsonBody) // 添加 JSON 參數
                .post(url, true) // true 表示使用 JSON 格式
                .sync();

        // 驗證回應
        assertNotNull(response, "回應不應為空");
        assertEquals(201, response.getHttpStatusCode(), "HTTP 狀態碼應為 201 (Created)");

        // 驗證回應內容包含預期的資料
        String responseData = response.getResultData();
        assertNotNull(responseData, "回應資料不應為空");
        System.out.println("POST JSON 請求回應: " + responseData);

        // 解析 JSON 回應
        JsonNode jsonResponse = JacksonUtils.toJsonNode(responseData);
        assertNotNull(jsonResponse.get("id"), "回應應包含 id 欄位");
        assertEquals("測試標題", jsonResponse.get("title").asText(), "回應應包含正確的 title");
        assertEquals("這是測試內容文字", jsonResponse.get("body").asText(), "回應應包含正確的 body");
    }

    @Test
    @DisplayName("測試 POST 請求 (表單)")
    public void testPostRequestForm() throws IOException {
        // 建立請求 URL - 建立 post 資料
        String url = TEST_API_BASE_URL + "/posts";

        // 建立表單參數
        Map<String, String> formParams = new HashMap<>();
        formParams.put("title", "表單測試標題");
        formParams.put("body", "這是表單提交的測試內容");
        formParams.put("userId", "2");

        // 執行 POST 請求並同步取得回應
        ApiResponse response = TestOkHttpUtils.builder()
                .addBrowserHeader()
                .addParam(formParams) // 添加表單參數
                .post(url, false) // false 表示使用表單格式
                .sync();

        // 驗證回應
        assertNotNull(response, "回應不應為空");
        assertEquals(201, response.getHttpStatusCode(), "HTTP 狀態碼應為 201 (Created)");

        // 驗證回應內容包含預期的資料
        String responseData = response.getResultData();
        assertNotNull(responseData, "回應資料不應為空");
        System.out.println("POST 表單請求回應: " + responseData);

        // 解析 JSON 回應
        JsonNode jsonResponse = JacksonUtils.toJsonNode(responseData);
        assertNotNull(jsonResponse.get("id"), "回應應包含 id 欄位");
    }

    @Test
    @DisplayName("測試非同步 GET 請求")
    public void testAsyncGetRequest() {
        // 建立請求 URL
        String url = TEST_API_BASE_URL + "/posts/1";

        // 使用回調方式執行非同步 GET 請求
        TestOkHttpUtils.builder()
                .addBrowserHeader()
                .get(url)
                .async(new TestOkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(okhttp3.Call call, String data) throws IOException {
                        assertNotNull(data, "回應資料不應為空");
                        System.out.println("非同步 GET 請求回應: " + data);

                        // 解析 JSON 回應
                        JsonNode jsonResponse = JacksonUtils.toJsonNode(data);
                        assertNotNull(jsonResponse.get("id"), "回應應包含 id 欄位");
                        assertNotNull(jsonResponse.get("title"), "回應應包含 title 欄位");
                    }

                    @Override
                    public void onFailure(okhttp3.Call call, String errorMsg) {
                        fail("非同步請求不應失敗: " + errorMsg);
                    }
                });

        // 等待非同步請求完成
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package com.cheng.linegroup.utils;

import com.cheng.linegroup.utils.dto.ApiResponse;
import com.cheng.linegroup.utils.dto.IpProxy;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 測試工具類 - 專用於單元測試
 * <p>
 * 此類別是 OkHttpUtils 的簡化版本，專為測試使用而設計，移除了對 Spring 上下文的依賴。
 *
 * @author cheng
 * @since 2025/06/07
 */
@Slf4j
public class TestOkHttpUtils {

    public static final String CHARSET_BIG5 = "Big5";

    // 測試專用，直接設定為 X-Line-Request-Id
    private final String requestIdHeader = "X-Line-Request-Id";
    private static volatile OkHttpClient okHttpClient = null;
    private static volatile Semaphore semaphore = null;
    private Map<String, String> headerMap;
    private Map<String, String> paramMap;
    private ObjectNode paramObj;
    private Request.Builder request;

    TestOkHttpUtils() {
        init(null);
    }

    private TestOkHttpUtils(IpProxy ipProxy) {
        init(ipProxy);
    }

    private void init(IpProxy ipProxy) {
        if (okHttpClient == null) {
            synchronized (TestOkHttpUtils.class) {
                if (okHttpClient == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                    TrustManager[] trustManagers = buildTrustManagers();
                    OkHttpClient.Builder builder =
                            new OkHttpClient.Builder()
                                    .addInterceptor(logging)
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(8, TimeUnit.SECONDS)
                                    .readTimeout(8, TimeUnit.SECONDS)
                                    .sslSocketFactory(
                                            createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                                    .hostnameVerifier((hostName, session) -> true)
                                    .retryOnConnectionFailure(true)
                                    .dispatcher(new Dispatcher(new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000))))
                                    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
                    if (ipProxy != null) {
                        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProxy.getIp(), ipProxy.getPort())));
                    }
                    okHttpClient = builder.build();
                    addHeader(HttpHeaders.ACCEPT, "*/*");
                    addHeader(HttpHeaders.CONNECTION, "keep-alive");
                }
            }
        }
    }

    /**
     * 控制多個執行緒對共用資源的訪問
     */
    private static Semaphore getSemaphoreInstance() {
        synchronized (TestOkHttpUtils.class) {
            if (semaphore == null) {
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    public static TestOkHttpUtils builder() {
        return new TestOkHttpUtils();
    }

    public static TestOkHttpUtils builder(IpProxy ipProxy) {
        return new TestOkHttpUtils(ipProxy);
    }

    public TestOkHttpUtils addParam(String key, String value) {
        if (paramObj == null) {
            paramObj = JacksonUtils.genJsonObject();
        }
        paramObj.put(key, value);
        return this;
    }

    public TestOkHttpUtils addParam(String key, Object value) {
        if (paramObj == null) {
            paramObj = JacksonUtils.genJsonObject();
        }
        paramObj.putPOJO(key, value);
        return this;
    }

    public TestOkHttpUtils addParam(ObjectNode params) {
        if (paramObj == null) {
            paramObj = JacksonUtils.genJsonObject();
        }
        if (params != null) {
            paramObj.setAll(params);
        }
        return this;
    }

    public TestOkHttpUtils addParam(Map<String, String> params) {
        if (paramMap == null) {
            paramMap = new HashMap<>(10);
        }
        if (params != null) {
            paramMap.putAll(params);
        }
        return this;
    }

    public TestOkHttpUtils addHeader(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Header key and value must not be null");
        }
        if (headerMap == null) {
            headerMap = new HashMap<>(10);
        }
        headerMap.put(key, value);
        return this;
    }

    public TestOkHttpUtils addBrowserHeader() {
        addHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 OPR/93.0.0.0");
        return this;
    }

    public TestOkHttpUtils addLineAuthHeader(String token) {
        addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        return this;
    }

    public TestOkHttpUtils get(String url) {
        String u = combinedUrlParams(url);
        log.info("===> [GET] API Url:{}", u);
        request.url(u);
        return this;
    }

    public String combinedUrlParams(String url) {
        request = new Request.Builder().get();
        Map<String, String> map;
        if (paramMap == null || paramMap.isEmpty()) {
            map = JacksonUtils.toMap(JacksonUtils.toJsonString(paramObj));
        } else {
            map = paramMap;
        }
        String encodingParams = ApiUtils.encodingParams(map);
        encodingParams = (StringUtils.isBlank(encodingParams) ? "" : "?") + encodingParams;
        return String.format("%s%s", url, encodingParams);
    }

    /**
     * 預設使用json submit，若要設定Form submit則需使用{@link TestOkHttpUtils#post(String, boolean)}，並將布林參數設為false
     */
    public TestOkHttpUtils post(String url) {
        return post(url, true);
    }

    /**
     * 使用順序<br>
     * {@link #addHeader(String, String)}/{@link #addParam(ObjectNode)} -> <br>
     * {@link #post(String)} -> <br>
     * {@link #sync()}/{@link #async()}
     * <p>
     *
     * @param isJsonPost true:Json, false:Form
     */
    public TestOkHttpUtils post(String url, boolean isJsonPost) {
        log.info("===>[POST] API Url:{}", url);
        RequestBody requestBody;
        if (isJsonPost) {
            String json = JacksonUtils.toJsonString(paramObj);
            log.info("===>[ParamJson]:{}", json);
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            requestBody = RequestBody.create(json, mediaType);
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            Map<String, String> params;
            if (paramMap == null || paramMap.isEmpty()) {
                params = JacksonUtils.toMap(JacksonUtils.toJsonString(paramObj));
            } else {
                params = paramMap;
            }
            if (MapUtils.isNotEmpty(params)) {
                params.forEach((k, v) -> {
                    log.info("key:{}, value:{}", k, v);
                    Optional.ofNullable(v).ifPresent(val -> formBody.add(k, val));
                });
            }
            FormBody fb = formBody.build();
            requestBody = fb;
            log.info("===>[FormBody]:{}", fb);
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    public ApiResponse syncReadCsv() {
        setHeader(request);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Apache Commons CSV
            try (CSVParser csvParser = new CSVParser(new InputStreamReader(
                    Objects.requireNonNull(response.body()).byteStream(), CHARSET_BIG5), CSVFormat.DEFAULT)) {
                return ApiResponse.builder()
                        .httpStatusCode(response.code())
                        .csvRecords(csvParser.getRecords()).build();
            }

        } catch (IOException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return ApiResponse.empty();
        }
    }

    public ApiResponse sync() {
        setHeader(request);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {

            int code = response.code();
            String lineRequestId = response.header(requestIdHeader);
            String contentType = response.header("Content-Type");

            ApiResponse apiResponse;
            if (contentType != null && contentType.contains("application/json")) {
                String data = Objects.requireNonNull(response.body()).string();
                apiResponse = ApiResponse.builder()
                        .httpStatusCode(code)
                        .lineRequestId(lineRequestId)
                        .resultData(data)
                        .contentType(contentType)
                        .build();
            } else {
                byte[] data = Objects.requireNonNull(response.body()).bytes();
                apiResponse = ApiResponse.builder()
                        .httpStatusCode(code)
                        .lineRequestId(lineRequestId)
                        .contentType(contentType)
                        .binaryData(data)
                        .build();
            }
            int httpStatusCode = apiResponse.getHttpStatusCode();
            if (httpStatusCode != HttpStatus.OK.value()) {
                log.info("===> API httpStatusCode:{} Response:{}", httpStatusCode, apiResponse);
            }
            return apiResponse;
        } catch (IOException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return ApiResponse.empty();
        }
    }

    public ApiResponse async() {
        StringBuilder buffer = new StringBuilder();
        ApiResponse.ApiResponseBuilder builder = ApiResponse.builder();
        setHeader(request);
        okHttpClient
                .newCall(request.build())
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                buffer.append("ERROR:").append(e.getMessage());
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response)
                                    throws IOException {
                                try {
                                    builder.httpStatusCode(response.code());
                                    buffer.append(Objects.requireNonNull(response.body()).string());
                                } finally {
//                                    getSemaphoreInstance().release();
                                }
                            }
                        });

        // async 版本無需使用 Semaphore，直接返回結果 (如果非同步操作需要同步完成，則應啟用)
//        try {
//            getSemaphoreInstance().acquire();
//        } catch (InterruptedException e) {
//            log.info("ERR:{}", ExceptionUtils.getStackTrace(e));
//            Thread.currentThread().interrupt();
//        }

        return builder.resultData(buffer.toString()).build();
    }

    public void async(ICallBack callBack) {
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        callBack.onFailure(call, e.getMessage());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response)
                            throws IOException {
                        callBack.onSuccessful(call, Objects.requireNonNull(response.body()).string());
                    }
                });
    }

    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            headerMap.forEach(request::addHeader);
        }
    }

    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
        }
        return ssfFactory;
    }

    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    public interface ICallBack {
        void onSuccessful(Call call, String data) throws IOException;

        void onFailure(Call call, String errorMsg);
    }
}

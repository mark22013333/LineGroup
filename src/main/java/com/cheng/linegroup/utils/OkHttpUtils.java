package com.cheng.linegroup.utils;

import com.cheng.linegroup.enums.LineHeader;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author cheng
 * @since 2023/12/3 12:01 PM
 **/
@Slf4j
public class OkHttpUtils {

    public static final String CHARSET_BIG5 = "Big5";

    private static volatile OkHttpClient okHttpClient = null;
    private static volatile Semaphore semaphore = null;
    private Map<String, String> headerMap;
    private ObjectNode paramObj;
    private Request.Builder request;

    private OkHttpUtils() {
        init(null);
    }

    private OkHttpUtils(IpProxy ipProxy) {
        init(ipProxy);
    }

    private void init(IpProxy ipProxy) {
        if (okHttpClient == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpClient == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                    TrustManager[] trustManagers = buildTrustManagers();
                    OkHttpClient.Builder builder =
                            new OkHttpClient.Builder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(8, TimeUnit.SECONDS)
                                    .readTimeout(8, TimeUnit.SECONDS)
                                    .sslSocketFactory(
                                            createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                                    .hostnameVerifier((hostName, session) -> true)
                                    .retryOnConnectionFailure(true)
//                                    .cookieJar(new OkHttpCookieManager())
                                    .addInterceptor(logging);
                    if (ipProxy != null) {
                        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProxy.getIp(), ipProxy.getPort())));
                    }
                    okHttpClient = builder.build();
                    addHeader(HttpHeaders.ACCEPT, "*/*");
                    addHeader(HttpHeaders.CONNECTION, "keep-alive");
//                    addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
                }
            }
        }
    }

    /**
     * 控制多個執行緒對共用資源的訪問
     */
    private static Semaphore getSemaphoreInstance() {
        synchronized (OkHttpUtils.class) {
            if (semaphore == null) {
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    /**
     * 若要使用代理IP需要使用{@link OkHttpUtils#builder(IpProxy)}
     */
    public static OkHttpUtils builder() {
        return new OkHttpUtils();
    }

    public static OkHttpUtils builder(IpProxy ipProxy) {
        return new OkHttpUtils(ipProxy);
    }

    public OkHttpUtils addParam(String key, String value) {
        paramObj.put(key, value);
        return this;
    }

    public OkHttpUtils addParam(String key, Object value) {
        paramObj.putPOJO(key, value);
        return this;
    }

    public OkHttpUtils addParam(ObjectNode params) {
        paramObj = params;
        return this;
    }

    public OkHttpUtils addHeader(String key, String value) {
        headerMap = Optional.ofNullable(headerMap).orElse(new HashMap<>(10));
        headerMap.put(key, value);
        return this;
    }

    public OkHttpUtils addBrowserHeader() {
        addHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 OPR/93.0.0.0");
        return this;
    }

    public OkHttpUtils addLinePushHeader(String token) {
        addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        return this;
    }

    public OkHttpUtils get(String url) {
        String u = combinedUrlParams(url);
        log.info("===> [GET] API Url:{}", u);
        request.url(u);
        return this;
    }

    public String combinedUrlParams(String url) {
        request = new Request.Builder().get();
        Map<String, String> map = JacksonUtils.toMap(JacksonUtils.toJsonString(paramObj));
        String encodingParams = ApiUtils.encodingParams(map);
        encodingParams = (StringUtils.isBlank(encodingParams) ? "" : "?") + encodingParams;
        return String.format("%s%s", url, encodingParams);
    }

    /**
     * 預設使用json submit，若要設定Form submit則需使用{@link OkHttpUtils#post(String, boolean)}，並將布林參數設為false
     */
    public OkHttpUtils post(String url) {
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
    public OkHttpUtils post(String url, boolean isJsonPost) {
        log.info("===>[POST] API Url:{}", url);
        RequestBody requestBody;
        if (isJsonPost) {
            String json = JacksonUtils.toJsonString(paramObj);
            log.info("===>[ParamJson]:{}", json);
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            requestBody = RequestBody.create(json, mediaType);
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            Map<String, String> paramMap = JacksonUtils.toMap(JacksonUtils.toJsonString(paramObj));
            if (MapUtils.isNotEmpty(paramMap)) {
                paramMap.forEach((k, v) -> {
                    log.info("===>[FormBody] key:{}, value:{}", k, v);
                    Optional.ofNullable(v).ifPresent(val -> formBody.add(k, val));
                });
            }
            requestBody = formBody.build();
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
            String lineRequestId = response.header(LineHeader.LINE_REQUEST_ID.getName());
            String data = Objects.requireNonNull(response.body()).string();
            ApiResponse apiResponse = ApiResponse.builder()
                    .httpStatusCode(code)
                    .lineRequestId(lineRequestId)
                    .resultData(data)
                    .build();
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
                                builder.httpStatusCode(response.code());
                                buffer.append(Objects.requireNonNull(response.body()).string());
                                getSemaphoreInstance().release();
                            }
                        });

        try {
            getSemaphoreInstance().acquire();
        } catch (InterruptedException e) {
            log.info("ERR:{}", ExceptionUtils.getStackTrace(e));
            Thread.currentThread().interrupt();
        }

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
        void onSuccessful(Call call, String data);

        void onFailure(Call call, String errorMsg);
    }

    public static void main(String[] args) {
        // 參數設定順序要照以下方式，非同步或同步執行要放最後
        String domain = "https://www.test.com";
        String u = ApiUtils.getUrl(domain, "/testAPI");
        testPost(u);

        // 非同步有兩個方式，一個有callback，另一個直接返回結果
        //        testPostAsync(u);
        //        testPostAsyncCallback(u);
    }

    private static void testPost(String url) {
        OkHttpUtils.builder().post(url)
                .addParam(JacksonUtils.genJsonObject().put("參數", 1))
                .sync();
    }

    private static void testPostAsync(String u) {
        ApiResponse async = OkHttpUtils.builder().get(u).async();
        System.out.println("async = " + async);
    }

    private static void testPostAsyncCallback(String u) {
        OkHttpUtils.builder().get(u).async(
                new ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        System.out.println("call = " + call);
                        System.out.println("data = " + data);
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        System.out.println("call = " + call);
                        System.out.println("errorMsg = " + errorMsg);
                    }
                });
    }
}

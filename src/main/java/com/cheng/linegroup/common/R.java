package com.cheng.linegroup.common;

import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Cheng
 * @since 2022/12/10 下午 04:01
 */
@Data
@Builder
@Accessors(chain = true)
@Schema(description = "API 回傳結果")
public class R implements BaseResponse, Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    @Schema(
            title = "code",
            description = "回應代號",
            format = "int32",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int code;

    @Schema(
            title = "msg",
            description = "回傳的訊息",
            format = "String",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "Success or Error",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String msg;

    @Schema(
            title = "data",
            description = "回傳的資料",
            format = "Object",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Object data;

    public static R success() {
        return success(null);
    }

    public static R success(Object data) {
        return R.builder()
                .code(ApiResult.SUCCESS.getCode())
                .msg(ApiResult.SUCCESS.getMsg())
                .data(data)
                .build();
    }

    public static R error() {
        return failed(ApiResult.ERROR);
    }

    public static R error(String msg, Object... args) {
        return create(ApiResult.ERROR.getCode(), msg, args);
    }

    public static R failed(ApiResult status, Object... args) {
        return create(status.getCode(), status.getMsg(), args);
    }

    public static R failed(BizException e) {
        return create(e.getCode(), e.getMessage());
    }

    public static R create(ApiResult api, Object data) {
        return R.builder().code(api.getCode()).msg(api.getMsg()).data(data).build();
    }

    public static R create(int code, String msg, Object data) {
        return R.builder().code(code).msg(msg).data(data).build();
    }

    private static R create(int code, String msg, Object... args) {
        if (args.length > 0) {
            return R.builder().code(code).msg(String.format(msg, args)).build();
        } else {
            return R.builder().code(code).msg(msg).build();
        }
    }
}

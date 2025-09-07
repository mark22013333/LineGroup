package com.cheng.linegroup.exception;

import com.cheng.linegroup.common.R;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.services.LineNotifyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheng
 * @since 2023/12/3 1:40 PM
 **/
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LineNotifyService lineNotifyService;

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            UnsatisfiedServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R> badRequestException(Exception e) {
        log.error("==> 參數錯誤:{}, ERR:{}", e.getMessage(), ExceptionUtils.getStackTrace(e));
        return ResponseEntity.badRequest().body(R.failed(BizException.error(HttpStatus.BAD_REQUEST.value(), e.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(
                "參數檢驗錯誤:{}, 異常類型:{}, ERR:{}",
                e.getMessage(),
                e.getClass(),
                ExceptionUtils.getStackTrace(e));
        BindingResult result = e.getBindingResult();
        Map<String, String> errMap = new HashMap<>();
        result.getFieldErrors().forEach(err -> errMap.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(R.create(ApiResult.VALIDATION_ERROR, errMap));
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<R> bizException(BizException e) {
        log.error("業務邏輯處理異常:{}, ERR:{}", e.getMessage(), ExceptionUtils.getStackTrace(e));
        return ResponseEntity.internalServerError().body(R.failed(e));
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<R> sqlException(SQLException e) {
        log.error("資料庫異常:{}", ExceptionUtils.getStackTrace(e));
//        lineNotifyService.pushMessage(
//                List.of(),
//                LineMessageContent.builder()
//                        .messageSendType(SQL_ERROR)
//                        .customMessage(e.getMessage())
//                        .build(),
//                DEVELOPER);
        return ResponseEntity.internalServerError().body(R.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("參數異常:{}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(R.failed(BizException.error(HttpStatus.BAD_REQUEST.value(), e.getMessage())));
    }

    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<R> handleJsonProcessingException(JsonProcessingException e) {
        log.error("Json轉換異常:{}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(R.failed(BizException.error(HttpStatus.BAD_REQUEST.value(), e.getMessage())));
    }


    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<R> exception(Throwable t) {
        log.error("系統異常:{}", ExceptionUtils.getStackTrace(t));
        return ResponseEntity.internalServerError().body(R.error(t.getMessage()));
    }
}


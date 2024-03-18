package com.cheng.linegroup.dto;

import com.cheng.linegroup.common.contants.Sign;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author cheng
 * @since 2024/3/12 22:39
 **/
@Slf4j
public class BaseDto {

    @Override
    public String toString() {
        return toLikeJson().replaceAll(Sign.REGISTERED, "\"");
    }

    public String toLikeJson() {
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String s = StringUtils.EMPTY;
        try {
            s = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
        }

        if (StringUtils.EMPTY.equals(s)) {
            throw BizException.create(ApiResult.PARAM_ERROR, "param is empty");
        }

        return s.replace("\"", Sign.REGISTERED);
    }
}

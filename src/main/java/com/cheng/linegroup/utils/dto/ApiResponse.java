package com.cheng.linegroup.utils.dto;

import com.cheng.linegroup.utils.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;

/**
 * @author Cheng
 * @since 2022/9/9 下午 02:40
 **/
@Data
@Builder
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private int httpStatusCode;
    private String lineRequestId;
    private String resultData;
    private JsonNode previewData;
    // Apache Commons CSV
    private List<CSVRecord> csvRecords;

    public ApiResponse preview() throws IOException {
        return this.setPreviewData(JacksonUtils.toJsonNode(this.getResultData()));
    }

    public static ApiResponse empty(){
        return ApiResponse.builder().build();
    }

}

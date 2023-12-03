package com.cheng.linegroup.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author cheng
 * @since 2023/12/3 5:47 PM
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LineGroupResponse {
    private String groupId;
    private String userId;
    private String displayName;
    private String groupName;
    private String pictureUrl;
}

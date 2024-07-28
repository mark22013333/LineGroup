package com.cheng.linegroup.service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Cheng
 * @since 2024/7/28 18:09
 **/
@Data
@Builder
public class LineUserDto {
    private String displayName;
    private String userId;
    private String language;
    private String pictureUrl;
    private String statusMessage;
}

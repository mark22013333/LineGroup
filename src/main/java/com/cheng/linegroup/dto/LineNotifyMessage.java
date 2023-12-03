package com.cheng.linegroup.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URL;

/**
 * @author cheng
 * @since 2023/12/3 2:43 PM
 **/
@Data
@Accessors(chain = true)
public class LineNotifyMessage {
    private String message;
    private URL imageUrl;

}

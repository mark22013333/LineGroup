package com.cheng.linegroup.dto.request;

import com.cheng.linegroup.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cheng
 * @since 2024/3/12 22:28
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SignUpOrIn extends BaseDto {
    private String username;
    private String nickname;
    private String phone;
    private String password;
    private String email;
}

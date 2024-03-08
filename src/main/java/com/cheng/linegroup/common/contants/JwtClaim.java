package com.cheng.linegroup.common.contants;

/**
 * @author cheng
 * @since 2024/3/7 23:31
 **/
public interface JwtClaim {

    String ISSUER = "iss";
    String SUBJECT = "sub";
    String AUDIENCE = "aud";
    String EXPIRES_AT = "exp";
    String NOT_BEFORE = "nbf";
    String ISSUED_AT = "iat";
    String JWT_ID = "jti";
    String USER_ID = "userId";

    /**
     * 權限（角色集合）
     */
    String AUTHORITIES = "authorities";
}

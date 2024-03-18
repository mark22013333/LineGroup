package com.cheng.linegroup.common.contants;

/**
 * @author cheng
 * @since 2024/3/7 23:31
 **/
public final class JwtClaim {

    private JwtClaim() {

    }

    public static final String ISSUER = "iss";
    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String EXPIRES_AT = "exp";
    public static final String NOT_BEFORE = "nbf";
    public static final String ISSUED_AT = "iat";
    public static final String JWT_ID = "jti";
    public static final String USER_ID = "userId";

    /**
     * 權限（角色集合）
     */
    public static final String AUTHORITIES = "authorities";
}

package com.cheng.linegroup.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/3/11 21:19
 **/
@Component
@RequiredArgsConstructor
public class RedisUtils {

    public final RedisTemplate<String, ?> redisTemplate;

}

package com.cheng.linegroup.redis;

import com.cheng.linegroup.entity.LineUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author cheng
 * @since 2024/2/28 23:48
 **/
@SpringBootTest
@Slf4j
class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testRedisSerializer() {
        LineUser user = new LineUser();
        user.setId(2);
        user.setNickname("試試水溫");
        redisTemplate.opsForValue().set("user", user);

        LineUser userCache = (LineUser) redisTemplate.opsForValue().get("user");
        log.info("userCache:{}", userCache);

    }
}

package com.cheng.linegroup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author cheng
 * @since 2024/2/28 23:53
 **/

@Configuration
public class RedisConfig {

    /**
     * 自定義 RedisTemplate，修改 Redis 序列化方式，預設是 JdkSerializationRedisSerializer
     *
     * @param redisConnectionFactory {@link RedisConnectionFactory}
     * @return {@link RedisTemplate}
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // String 序列化方式作為 key 的序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // JSON 序列化方式作為 value 的序列化器
        redisTemplate.setValueSerializer(RedisSerializer.json());

        // hash 的 key 也使用 String 序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        // hash 的 value 也使用 JSON 序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}

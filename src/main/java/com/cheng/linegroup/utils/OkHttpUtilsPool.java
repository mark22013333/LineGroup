package com.cheng.linegroup.utils;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * @author Cheng
 * @since 2024/12/11 22:05
 **/
public class OkHttpUtilsPool {
    private static final GenericObjectPool<OkHttpUtils> POOL;


    static {
        GenericObjectPoolConfig<OkHttpUtils> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(50); // 最大物件數量
        config.setMinIdle(5);   // 最小閒置物件數量
        config.setMaxIdle(10);  // 最大閒置物件數量
        config.setBlockWhenExhausted(true); // 當物件用盡時是否阻塞
        config.setMaxWait(Duration.ofSeconds(15)); // 阻塞時最大等待時間
        config.setMinEvictableIdleDuration(Duration.ofMinutes(10)); // 最小閒置存活時間

        POOL = new GenericObjectPool<>(new BasePooledObjectFactory<>() {
            @Override
            public OkHttpUtils create() {
                return new OkHttpUtils();
            }

            @Override
            public PooledObject<OkHttpUtils> wrap(OkHttpUtils obj) {
                return new DefaultPooledObject<>(obj);
            }
        }, config);
    }

    public static OkHttpUtils borrow() throws Exception {
        return POOL.borrowObject();
    }

    public static void release(OkHttpUtils instance) {
        POOL.returnObject(instance);
    }
}

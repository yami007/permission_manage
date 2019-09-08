package com.permission.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@Service("redisPool")
@Slf4j
public class RedisPool {

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    public ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    public void safeClose(ShardedJedis shardedJedis) {
        try {
           if (shardedJedis != null) {
               shardedJedis.close();
           }
        } catch (Exception e) {
            log.error("return redis resource exception", e);
        }
    }
}

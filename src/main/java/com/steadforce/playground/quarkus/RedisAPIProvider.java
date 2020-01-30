package com.steadforce.playground.quarkus;

import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

/**
 * Showcase for CDI Producer.
 *
 * @author steadforce gmbh
 */
public class RedisAPIProvider {
    final Logger logger;

    @Inject
    RedisAPIProvider(Logger logger) {
        this.logger = logger;
    }

    @Produces
    @ApplicationScoped
    public RedisAPI createRedisAPI(Redis redis) {
        logger.info("Producing redis connection");
        RedisAPI redisAPI = RedisAPI.api(redis);
        return redisAPI;
    }

    void close(@Disposes RedisAPI redisAPI) {
        logger.info("Closing redis api");
        redisAPI.shutdown(new ArrayList<>(0), responseAsyncResult -> {
            if (responseAsyncResult.succeeded()) {
                logger.info("Redis api successfully closed");
            }
        });
    }
}

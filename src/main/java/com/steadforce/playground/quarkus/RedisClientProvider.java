package com.steadforce.playground.quarkus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;

/**
 * Showcase for CDI Producer.
 *
 * @author steadforce gmbh
 */
@Dependent
public class RedisClientProvider {

    final Logger logger;

    final RedisNodeUriProvider redisNodeUriProvider;

    final Vertx vertx;

    @Inject
    RedisClientProvider(Logger logger, RedisNodeUriProvider redisNodeUriProvider, Vertx vertx) {
        this.logger = logger;
        this.redisNodeUriProvider = redisNodeUriProvider;
        this.vertx = vertx;
    }

    /**
     * Used by applicationscoped RedisAPIProvider producer, has to be dependent scoped to ensure that it is disposed
     * after the redis connection.
     *
     * @return wrapped redis client
     * @throws InterruptedException
     */
    @Produces
    public Redis createClient() throws InterruptedException {
        Redis redisClient = Redis.createClient(vertx, redisNodeUriProvider.getRedisNodeUris());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        redisClient.connect(connect -> {
            if (connect.failed()) {
                logger.error("Connecting to redis failed!", connect.cause());
            } else {
                logger.info("Redis connected.");
                countDownLatch.countDown();
            }
        });
        if (!countDownLatch.await(2, TimeUnit.SECONDS)) {
            logger.error("Connecting to redis timed out!");
            redisClient = null;
        }
        return redisClient;
    }

    void close(@Disposes Redis client) {
        logger.info("Closing redis client");
        client.close();
    }

}

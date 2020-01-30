package com.steadforce.playground.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.vertx.redis.client.Redis;

@Readiness
@ApplicationScoped
public class RedisHealthCheck implements HealthCheck {
    final RedisClientProvider clientProvider;

    final RedisAPIProvider connectionProvider;

    @Inject
    RedisHealthCheck(RedisClientProvider clientProvider, RedisAPIProvider connectionProvider) {
        this.clientProvider = clientProvider;
        this.connectionProvider = connectionProvider;
    }

    // vertx redis library uses asynchronous callbacks
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Redis connection health check");
        // Don't use injection for redis connection and client here, we want always new connection to check if redis is
        // reachable
        Redis client;
        HealthCheckResponse response;
        try {
            client = clientProvider.createClient();
            if (client != null) {
                response = responseBuilder.up().build();
                client.close();
            } else {
                response = responseBuilder.down().build();
            }
        } catch (InterruptedException e) {
            // should not happen
            response = responseBuilder.down().build();
        }

        return response;
    }
}

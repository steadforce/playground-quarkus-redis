package com.steadforce.playground.quarkus;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.redis.client.RedisOptions;

/**
 * Showcase for CDI Bean using microprofile configuration.
 *
 * @author steadforce gmbh
 */
@Dependent
public class RedisNodeUriProvider {

    @Inject
    @ConfigProperty(name = "REDIS_NODE_URIS", defaultValue = "redis://redis.example:30600,redis://redis.example:30610")
    String redisNodeUris;

    public RedisOptions getRedisNodeUris() {
        String[] redisUriStrings = redisNodeUris.split(",");
        RedisOptions redisOptions = new RedisOptions();
        for (String redisUri : redisUriStrings) {
            URI uri = URI.create(redisUri);
            redisOptions.addEndpoint(new SocketAddressImpl(uri.getPort(), uri.getHost()));
        }
        return redisOptions;
    }

}

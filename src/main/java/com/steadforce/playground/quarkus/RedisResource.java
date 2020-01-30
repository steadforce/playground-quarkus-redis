package com.steadforce.playground.quarkus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

@Path("/redis")
@Produces(MediaType.APPLICATION_JSON)
public class RedisResource {

    final RedisAPI connection;

    @Inject
    RedisResource(RedisAPI connection) {
        this.connection = connection;
    }

    @GET
    public void list(@Suspended AsyncResponse asyncResponse) {
        retrieveAllKeys(asyncResponse);
    }

    @GET
    @Path("/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public void value(@PathParam("key") String key, @Suspended AsyncResponse asyncResponse) {
        connection.dump(key, responseAsyncResult -> {
            if (responseAsyncResult.succeeded()) {
                asyncResponse.resume(responseAsyncResult.result().toString());
            } else {
                throw new RuntimeException("Redis server is unavailable");
            }
        });
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/{key}")
    public void add(@PathParam("key") String key, String value, @Suspended AsyncResponse asyncResponse) {
        connection.set(Arrays.asList(key, value), responseAsyncResult -> {
            if (responseAsyncResult.succeeded()) {
                retrieveAllKeys(asyncResponse);
            } else {
                throw new RuntimeException("Redis server is unavailable");
            }
        });
    }

    @DELETE
    @Path("/{key}")
    public void delete(@PathParam("key") String key, @Suspended AsyncResponse asyncResponse) {
        connection.del(Arrays.asList(key), responseAsyncResult -> {
            if (responseAsyncResult.succeeded()) {
                retrieveAllKeys(asyncResponse);
            } else {
                throw new RuntimeException("Redis server is unavailable");
            }
        });
    }

    private void retrieveAllKeys(AsyncResponse asyncResponse) {
        connection.keys("*", response -> {
            if (response.succeeded()) {
                List<String> values = new ArrayList<>();
                Iterator<Response> results = response.result().iterator();
                while (results.hasNext()) {
                    values.add(results.next().toString());
                }
                asyncResponse.resume(values);
            } else {
                throw new RuntimeException("Redis server is unavailable");
            }
        });
    }
}

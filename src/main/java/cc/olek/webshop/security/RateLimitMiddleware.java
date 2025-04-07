package cc.olek.webshop.security;

import cc.olek.webshop.auth.AuthenticationService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.redis.client.impl.RedisClient;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(0)
public class RateLimitMiddleware implements ContainerRequestFilter {
    @Inject
    RedisDataSource redisClient;

    @Inject
    ResourceInfo resourceInfo;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        IpRateLimit ipRateLimit = resourceInfo.getResourceMethod().getAnnotation(IpRateLimit.class);
        ValueCommands<String, Integer> rateLimits = redisClient.value(Integer.class);
        if (ipRateLimit == null) return;
        String userKey = "ratelimit_ip_" + request.remoteAddress().host();
        int perMinuteValue = ipRateLimit.value();
        if(perMinuteValue < rateLimits.incr(userKey)) {
            requestContext.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS).build());
        }
    }
}

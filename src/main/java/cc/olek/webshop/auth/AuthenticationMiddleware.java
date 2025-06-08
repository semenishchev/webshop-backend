package cc.olek.webshop.auth;

import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.util.JResponse;
import io.quarkus.security.Authenticated;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Middleware to authenticate users with Authentication header into API. This header will be passed by API users, including frontend.
 * Frontend will read Session value from cookies
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationMiddleware implements ContainerRequestFilter {

    @Inject
    UserContext userContext;

    @Inject
    ResourceInfo resourceInfo;

    @Inject
    AuthenticationService authenticationService;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        Method method = resourceInfo.getResourceMethod();

        boolean required = method != null && method.isAnnotationPresent(Authenticated.class);
        Class<?> caller = resourceInfo.getResourceClass();
        if(caller != null && caller.isAnnotationPresent(Authenticated.class)) {
            required = true;
        }

        String auth = context.getHeaderString("Authorization");
        if(auth == null) {
            if(required) context.abortWith(JResponse.json(401, "No auth header"));
            return;
        }

        if(auth.startsWith("Bearer ")) {
            auth = auth.substring("Bearer ".length());
        }

        UserSession session = authenticationService.findSession(auth);
        if(session == null) {
            if(required) context.abortWith(JResponse.json(401, "Session not found"));
            return;
        }

        if(session.isCookiePresent()) {
            Cookie sessionId = context.getCookies().get("ws-session_id");
            if(sessionId == null) {
                context.abortWith(JResponse.json(401, "Session cookie not found"));
                return;
            }
            if(!session.isValidCookie(sessionId.getValue())) {
                context.abortWith(JResponse.json(401, "Invalid session cookie"));
                return;
            }
        }

        String currentIp = request.remoteAddress().host();
        if(!Objects.equals(session.ipAddress, currentIp)) {
            authenticationService.updateIpAddress(session, currentIp);
        }

        userContext.setUser(session.user);
        userContext.setSession(session);
    }
}
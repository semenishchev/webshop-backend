package cc.olek.webshop.auth;

import io.quarkus.security.Authenticated;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;

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

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        Method method = resourceInfo.getResourceMethod();

        boolean required = method != null && method.isAnnotationPresent(Authenticated.class);

        Class<?> caller = resourceInfo.getResourceClass();
        if(caller != null && caller.isAnnotationPresent(Authenticated.class)) {
            required = true;
        }

        String auth = context.getHeaderString("Authorization");
        if(auth == null && required) {
            context.abortWith(Response.status(401).build());
            return;
        }

        if(auth != null && auth.startsWith("Basic ")) {
            auth = auth.substring("Basic ".length());
        }

        UserSession session = UserSession.find("sessionText", auth).firstResult();
        if(session == null) {
            if(required) context.abortWith(Response.status(401).build());
            return;
        }

        userContext.setUser(session.user);
        userContext.setSession(session);
    }
}
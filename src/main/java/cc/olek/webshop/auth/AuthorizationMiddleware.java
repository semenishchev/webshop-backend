package cc.olek.webshop.auth;

import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationMiddleware implements ContainerRequestFilter {
    @Inject
    UserContext userContext;
    @Inject
    ResourceInfo resourceInfo;
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        User user = userContext.getUser();
        if(user == null) return;
        AdminRequired requirement = resourceInfo.getResourceMethod().getAnnotation(AdminRequired.class);
        checker: if(requirement != null) {
            if(user.isSuperuser()) break checker;
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            return;
        }
        requirement = resourceInfo.getResourceClass().getAnnotation(AdminRequired.class);
        if(requirement != null) {
            if(user.isSuperuser()) return;
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }
}

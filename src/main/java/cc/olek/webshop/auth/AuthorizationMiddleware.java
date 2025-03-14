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
        // todo: perm
    }
}

package cc.olek.webshop.user;

import cc.olek.webshop.auth.UserContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@RunOnVirtualThread
public class UserResource {
    @Inject
    UserContext userContext;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    public User me() {
        return userContext.getUser();
    }

    @GET
    @Path("/user/{id}")
    public User getUser(@PathParam("id") String id) {
        try {
            return userService.findUserById(Long.parseLong(id));
        } catch (NumberFormatException ignored) {}
        return userService.findUserByEmail(id);
    }
}

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
public class UserService {
    @Inject
    UserContext userContext;

    @GET
    @Path("/me")
    public User me() {
        return userContext.getUser();
    }

    @GET
    @Path("/user/{id}")
    public User getUser(@PathParam("id") long id) {
        return User.findById(id);
    }
}

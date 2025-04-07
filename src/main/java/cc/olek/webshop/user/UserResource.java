package cc.olek.webshop.user;

import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user/{id}")
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
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") String id) {
        return parse(userContext.getUser(), id);
    }

    @PATCH
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") String id, UserProfile userProfile) {
        User actionOn = parse(userContext.getUser(), id);
        UserProfile existing = actionOn.getProfile();
        existing.merge(userProfile);
        userService.saveUser(actionOn);
        return Response.noContent().build();
    }

    private static User parse(User executor, String id) {
        if(id.equalsIgnoreCase("me")) {
            return executor;
        }

        // todo: permission lookup
        throw new UnauthorizedException();
    }
}

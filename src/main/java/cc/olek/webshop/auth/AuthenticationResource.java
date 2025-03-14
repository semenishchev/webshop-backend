package cc.olek.webshop.auth;

import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.user.UserService;
import cc.olek.webshop.user.UserSession;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/auth")
@RunOnVirtualThread
public class AuthenticationResource {

    @Inject
    Instance<UserContext> context;

    @Inject
    AuthenticationService authenticationService;
    @Inject
    UserService userService;

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(AuthenticationData data) {
        UserSession session = authenticationService.authenticate(data.email, data.password);
        if(session == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok().entity(session.sessionText).build();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(AuthenticationData data) {
        if (userService.findUserByEmail(data.email) != null) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        userService.createUser(data.email, data.password);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        UserSession session = context.get().getSession();
        if(session == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        authenticationService.logout(session);
        return Response.ok().build();
    }

    public record AuthenticationData(String email, String password, Optional<String> toptPassword) {}
}

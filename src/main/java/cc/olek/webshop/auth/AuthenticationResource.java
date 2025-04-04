package cc.olek.webshop.auth;

import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.user.UserService;
import cc.olek.webshop.user.UserSession;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Path("/auth")
@RunOnVirtualThread
public class AuthenticationResource {

    @Inject
    UserContext context;

    @Inject
    AuthenticationService authenticationService;
    @Inject
    UserService userService;

    @ConfigProperty(name = "api.domain")
    String domain;

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(AuthenticationData data) {
        UserSession session = authenticationService.authenticate(data.isBrowserSession, data.email, data.password, data.toptPassword.orElse(null));
        if(session == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("TOPT").build();
        }
        Response.ResponseBuilder response = Response.ok().entity(session.sessionText);
        if(data.isBrowserSession) {
            boolean localhost = domain.equals("localhost");
            response.cookie(
                new NewCookie.Builder("ws-session_id")
                    .expiry(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
                    .httpOnly(true)
                    .secure(true)
                    .comment("Session")
                    .sameSite(localhost ? NewCookie.SameSite.NONE : NewCookie.SameSite.STRICT)
                    .domain(localhost ? "localhost" : "." + domain)
                    .build()
            );
        }
        return response.build();
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
        UserSession session = context.getSession();
        if(session == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        authenticationService.logout(session);
        return Response.ok().build();
    }

    public record AuthenticationData(String email, boolean isBrowserSession, String password, Optional<String> toptPassword) {}
}

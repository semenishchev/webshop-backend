package cc.olek.webshop.auth;

import cc.olek.webshop.user.User;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

@Path("/auth")
@RunOnVirtualThread
public class AuthenticationService {

    @Inject
    Instance<UserContext> context;

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response login(AuthenticationData data) {
        User user = User.find("email", data.email).firstResult();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!user.verifyPassword(data.password)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        // todo: read config and session
        UserSession session = new UserSession();
        session.user = user;
        String sessionText = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt());
        session.sessionText = sessionText;
        session.persist();
        return Response.ok().entity(sessionText).build();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response register(AuthenticationData data) {
        if (User.find("email", data.email).firstResultOptional().isPresent()) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        User user = new User();
        user.email = data.email;
        user.setPassword(data.password);
        user.persist();
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
        session.delete();
        return Response.ok().build();
    }

    public record AuthenticationData(String email, String password) {}
}

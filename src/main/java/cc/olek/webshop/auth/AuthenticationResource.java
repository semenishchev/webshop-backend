package cc.olek.webshop.auth;

import cc.olek.webshop.service.EmailService;
import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.user.UserService;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.util.JResponse;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/auth")
@RunOnVirtualThread
public class AuthenticationResource {

    @Inject
    UserContext context;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    UserService userService;

    @Inject
    EmailService emails;

    @ConfigProperty(name = "api.domain")
    String domain;

    @ConfigProperty(name = "frontend.email-confirmation-redirect")
    String emailConfirmationRedirect;

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(AuthenticationData data) {
        String randomCookie = UUID.randomUUID().toString() + UUID.randomUUID();
        UserSession session = authenticationService.authenticate(
            data.email,
            data.password,
            data.toptPassword.orElse(null),
            data.isBrowserSession() ? randomCookie : null
        );
        if(session == null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity(Map.of())
                .build();
        }
        Response.ResponseBuilder response = Response.ok().entity(Map.of("session", session.sessionText, "expiresAt", session.expiresAt));
        if(data.isBrowserSession) {
            response.cookie(createAuthCookie(randomCookie, false));
        }
        return response.build();
    }

    private NewCookie createAuthCookie(String randomCookie, boolean clear) {
        boolean localhost = domain.startsWith("localhost");
        var builder = new NewCookie.Builder("ws-session_id")
            .expiry(clear ? new Date(0) : Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
            .httpOnly(true)
            .comment(null)
            .path("/")
            .value(randomCookie)
            .sameSite(localhost ? NewCookie.SameSite.LAX : NewCookie.SameSite.STRICT)
            .domain(null);
        if(clear) {
            builder.maxAge(0);
        }
        if(!localhost) {
            builder.secure(true);
        }
        return builder.build();
    }

    @GET
    @Path("/confirm-email")
    public Response confirmRegistration(@QueryParam("token") String confirmationToken) {
        AuthenticationService.ProcessedRegistrationRequest request = authenticationService.fetchRegistrationRequest(confirmationToken);
        if(request == null) return Response.status(Response.Status.NOT_FOUND).build();
        authenticationService.confirmRegistration(request);

        return Response.seeOther(URI.create(emailConfirmationRedirect)).build();
    }

    @GET
    @Path("/check-email")
    public Response checkEmailVerification(@QueryParam("email") String email) {
        if (authenticationService.hasRegistrationVerification(email)) {
            return Response.seeOther(URI.create(emailConfirmationRedirect)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(AuthenticationService.RegistrationRequest data) {
        if (userService.findUserByEmail(data.email()) != null) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        AuthenticationService.ProcessedRegistrationRequest request = authenticationService.initiateRegistration(
            data.email(),
            data.password(),
            data.profile()
        );
        if(request == null) {
            return Response.status(Response.Status.CONFLICT).build(); // sanity check
        }
        emails.sendEmailVerification(data.email(), "https://" + domain + "/auth/confirm-email?token=" + request.emailConfirmationToken());
        return Response.status(Response.Status.NO_CONTENT).build();
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
        return Response.ok()
            .cookie(createAuthCookie("", true))
            .build();
    }

    public record AuthenticationData(String email, boolean isBrowserSession, String password, Optional<String> toptPassword) {}
}

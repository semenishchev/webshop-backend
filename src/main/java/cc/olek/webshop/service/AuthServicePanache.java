package cc.olek.webshop.service;

import cc.olek.webshop.auth.AuthenticationService;
import cc.olek.webshop.config.UsersConfig;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import io.quarkus.security.UnauthorizedException;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class AuthServicePanache implements AuthenticationService {
    @Inject
    UserService userService;

    @Inject
    UsersConfig config;

    @Context
    HttpServerRequest request;

    @Override
    @Transactional
    public UserSession authenticate(String email, String password) {
        User user = userService.findUserByEmail(email);
        // return null to give out generic unauthorised message
        if (user == null) {
            return null;
        }
        if (!user.verifyPassword(password)) {
            return null;
        }
        if (UserSession.count("user = ?1 and expired = false", user) >= config.maxSessionsPerUser()) {
            throw new UnauthorizedException("Too many sessions per User");
        }
        String usedIpAddr = request.remoteAddress().host();
        if (UserSession.count("ipAddress = ?1 and expired = false", usedIpAddr) >= config.maxSessionsPerIp()) {
            throw new UnauthorizedException("Too many sessions per IP");
        }

        UserSession session = new UserSession();
        session.user = user;
        session.sessionText = UUID.randomUUID().toString();
        session.expiresAt = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        session.ipAddress = usedIpAddr;
        session.persist();
        return session;
    }

    @Override
    public void logout(UserSession session) {
        UserSession.deleteById(session.id);
    }

    @Override
    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userService.findUserByEmail(email);
        user.setPassword(newPassword);
        User.getEntityManager().merge(user);
        UserSession.delete("user", user);
    }

    @Override
    public UserSession findSession(String sessionText) {
        return UserSession.find("sessionText", sessionText).firstResult();
    }

    @Override
    @Transactional
    public void updateIpAddress(UserSession session, String ipAddress) {
        session.ipAddress = ipAddress;
        UserSession.getEntityManager().merge(session);
    }
}

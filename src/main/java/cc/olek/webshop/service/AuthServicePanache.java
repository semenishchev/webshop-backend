package cc.olek.webshop.service;

import cc.olek.webshop.auth.AuthenticationService;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class AuthServicePanache implements AuthenticationService {
    @Inject
    UserService userService;

    @Override
    @Transactional
    public UserSession authenticate(String email, String password) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return null;
        }
        if (!user.verifyPassword(password)) {
            return null;
        }
        // todo: read config and verify against values such as max sessions per user, max sessions per ip and other potential validations
        UserSession session = new UserSession();
        session.user = user;
        session.sessionText = UUID.randomUUID().toString();
        session.expiresAt = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        session.ipAddress = UUID.randomUUID().toString();
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

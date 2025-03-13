package cc.olek.webshop.service;

import cc.olek.webshop.auth.AuthenticationService;
import cc.olek.webshop.auth.UserSession;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class AuthServicePanache implements AuthenticationService {
    @Inject
    UserService userService;

    @Override
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
        session.persist();
        return session;
    }

    @Override
    public void logout(UserSession session) {
        UserSession.deleteById(session.id);
    }
}

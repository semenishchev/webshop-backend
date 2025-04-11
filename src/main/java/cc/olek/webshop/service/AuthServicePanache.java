package cc.olek.webshop.service;

import cc.olek.webshop.auth.AuthenticationService;
import cc.olek.webshop.config.UsersConfig;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Context;
import org.mindrot.jbcrypt.BCrypt;

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

    @Inject
    TwoFactorService twoFactorService;

    @Inject
    RedisDataSource redis;

    @Override
    @Transactional
    public UserSession authenticate(String email, String password, String twoFactorCode, String cookie) {
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!user.verifyPassword(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (UserSession.count("user = ?1 and expiresAt < CURRENT_TIMESTAMP", user) >= config.maxSessionsPerUser()) {
            throw new UnauthorizedException("Too many sessions per User");
        }
        String usedIpAddr = request.remoteAddress().host();
        if (UserSession.count("ipAddress = ?1 and expiresAt < CURRENT_TIMESTAMP", usedIpAddr) >= config.maxSessionsPerIp()) {
            throw new UnauthorizedException("Too many sessions per IP");
        }

        checker: if(user.getTwoFactorSecret() != null) {
            if(twoFactorCode == null) return null;
            if(twoFactorService.isValid(user, twoFactorCode)) break checker;
            throw new ForbiddenException();
        }

        UserSession session = new UserSession();
        session.user = user;
        session.sessionText = UUID.randomUUID().toString();
        if(cookie != null) {
            session.setCookieSession(cookie);
        }
        session.expiresAt = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        session.ipAddress = usedIpAddr;
        session.persist();
        return session;
    }

    @Override
    public boolean hasRegistrationVerification(String email) {
        return redis.key().exists("e_registration_request_" + email);
    }

    @Override
    public RegistrationRequest initiateRegistration(String email, String password) {
        String emailKey = "e_registration_request_" + email;
        if (redis.key().exists(emailKey)) {
            return null;
        }
        if(userService.findUserByEmail(email) != null) {
            return null;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String token = UUID.randomUUID().toString();
        RegistrationRequest request = new RegistrationRequest(
            email,
            hashedPassword,
            token
        );

        String tokenKey = "t_registration_request_" + token;
        redis.value(RegistrationRequest.class).set(
            emailKey,
            request,
            new SetArgs().ex(600) // after 10 minutes
        );
        redis.value(RegistrationRequest.class).set(
            tokenKey,
            request,
            new SetArgs().ex(600) // after 10 minutes
        );
        return request;
    }

    @Override
    public RegistrationRequest fetchRegistrationRequest(String token) {
        return redis.value(RegistrationRequest.class).get("t_registration_request_" + token);
    }

    @Override
    public void terminateRegistration(String email) {
        RegistrationRequest request = redis.value(RegistrationRequest.class).getdel("e_registration_request_" + email);
        if(request == null) return;
        redis.key().del("t_registration_request_" + request.emailConfirmationToken());
    }

    @Override
    public void confirmRegistration(RegistrationRequest request) {
        KeyCommands<String> keys = redis.key();
        userService.createUserRaw(request.email(), request.hashedPassword());
        keys.del("t_registration_request_" + request.emailConfirmationToken(), "e_registration_request_" + request.email());
    }

    @Override
    public TwoFactorService.InitiationData initiateTwoFactorAuthentication(User user) {
        if(user.getTwoFactorSecret() != null) {
            throw new ForbiddenException();
        }
        TwoFactorService.InitiationData initiationData;
        try {
            initiationData = twoFactorService.createInitiationData(user);
        } catch (QrGenerationException e) {
            Log.errorf(e,"Failed to generate a two factor secret for user %i %s", user.id, user.getEmail());
            throw new ServerErrorException("Failed to generate TOPT data for you", 500);
        }
        twoFactorService.saveToInitCache(user, initiationData, 120); // seconds
        return initiationData;
    }

    @Override
    public void terminateTwoFactorInitiation(User user) {
        twoFactorService.removeCached(user);
    }

    @Override
    public boolean confirmTwoFactor(User user, String code) {
        TwoFactorService.InitiationData data = twoFactorService.getCached(user);
        if(data == null) {
            throw new NotFoundException();
        }
        boolean flag = twoFactorService.isValidRaw(data.secret(), code);
        if(flag) {
            user.setTwoFactorSecret(data.secret());
            twoFactorService.removeCached(user);
            userService.saveUser(user);
        }
        return flag;
    }

    @Override
    public void logout(UserSession session) {
        UserSession.deleteById(session.id);
    }

    @Override
    @Transactional
    public void changePassword(User user, String newPassword, String toptCode) {
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

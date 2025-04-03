package cc.olek.webshop.service;

import cc.olek.webshop.auth.AuthenticationService;
import cc.olek.webshop.config.UsersConfig;
import cc.olek.webshop.user.UserSession;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
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

    @Inject
    TwoFactorService twoFactorService;

    @Inject
    RedisDataSource redisClient;

    @Override
    @Transactional
    public UserSession authenticate(String email, String password) {
        return doAuthenticate(email, password, null);
    }

    @Override
    public UserSession authenticate(String email, String password, String twoFactorCode) {
        return doAuthenticate(email, password, twoFactorCode);
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
        redisClient.value(TwoFactorService.InitiationData.class).set(user.getEmail(), initiationData);
        return initiationData;
    }

    @Override
    public boolean confirmTwoFactor(User user, String code) {
        TwoFactorService.InitiationData data = redisClient.value(TwoFactorService.InitiationData.class).get(user.getEmail());
        if(data == null) {
            throw new NotFoundException();
        }
        return twoFactorService.isValidRaw(data.secret(), code);
    }

    private UserSession doAuthenticate(String email, String password, String twoFactorCode) {
        User user = userService.findUserByEmail(email);
        // return null to give out generic unauthorised message
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!user.verifyPassword(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (UserSession.count("user = ?1 and expired = false", user) >= config.maxSessionsPerUser()) {
            throw new UnauthorizedException("Too many sessions per User");
        }
        String usedIpAddr = request.remoteAddress().host();
        if (UserSession.count("ipAddress = ?1 and expired = false", usedIpAddr) >= config.maxSessionsPerIp()) {
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

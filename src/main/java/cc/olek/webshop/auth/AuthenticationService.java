package cc.olek.webshop.auth;

import cc.olek.webshop.service.TwoFactorService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserSession;

public interface AuthenticationService {
    /**
     * Authenticates a user
     * Double auth parameter means it will generate a cookie and a session as 2 separate texts. Authentication will consist of API server receiving both params.
     * Should be true if request is made from the browser and false if frontend is some other kind of application
     * @param doubleAuth Enables duplicate verification for a cookie
     * @param email Email
     * @param password Password
     * @param twoFactor 2-factor code if present. If not present and required, will return null
     * @return UserSession if everything is valid, null if 2fa code is invalid
     * @throws io.quarkus.security.UnauthorizedException If credentials don't match
     */
    UserSession authenticate(boolean doubleAuth, String email, String password, String twoFactor);
    TwoFactorService.InitiationData initiateTwoFactorAuthentication(User user);
    void terminateTwoFactorInitiation(User user);
    boolean confirmTwoFactor(User user, String code);
    void changePassword(User user, String newPassword, String twoFactorCode);
    void logout(UserSession session);

    UserSession findSession(String sessionText);
    void updateIpAddress(UserSession session, String ipAddress);
}

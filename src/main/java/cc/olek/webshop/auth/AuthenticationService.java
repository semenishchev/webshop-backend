package cc.olek.webshop.auth;

import cc.olek.webshop.service.TwoFactorService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserSession;

public interface AuthenticationService {
    /**
     * Authenticates with a session
     * @param email Email of the user
     * @param password Password
     * @return Null if the authentication method didn't work (requires 2fa)
     * @throws io.quarkus.security.UnauthorizedException If credentials don't match
     */
    UserSession authenticate(String email, String password);

    /**
     * Authenticates with a 2fa
     * @param email Email
     * @param password Password
     * @param twoFactor Current code
     * @return UserSession if everything is valid, null if 2fa code is invalid
     * @throws io.quarkus.security.UnauthorizedException If credentials don't match
     */
    UserSession authenticate(String email, String password, String twoFactor);
    TwoFactorService.InitiationData initiateTwoFactorAuthentication(User user);
    boolean confirmTwoFactor(User user, String code);
    void changePassword(User user, String newPassword, String twoFactorCode);
    void logout(UserSession session);

    UserSession findSession(String sessionText);
    void updateIpAddress(UserSession session, String ipAddress);
}

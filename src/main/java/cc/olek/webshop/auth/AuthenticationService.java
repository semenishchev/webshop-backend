package cc.olek.webshop.auth;

import cc.olek.webshop.service.TwoFactorService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserProfile;
import cc.olek.webshop.user.UserSession;

public interface AuthenticationService {
    /**
     * Authenticates a user
     * Double auth parameter means it will generate a cookie and a session as 2 separate texts. Authentication will consist of API server receiving both params.
     * Should be true if request is made from the browser and false if frontend is some other kind of application
     * Enabled by providing cookie parameter
     * @param email Email
     * @param password Password
     * @param twoFactor 2-factor code if present. If not present and required, will return null
     * @param cookie Cookie to validate against
     * @return UserSession if everything is valid, null if 2fa code is invalid
     * @throws io.quarkus.security.UnauthorizedException If credentials don't match
     */
    UserSession authenticate(String email, String password, String twoFactor, String cookie);

    ProcessedRegistrationRequest initiateRegistration(String email, String password, UserProfile profile);
    ProcessedRegistrationRequest fetchRegistrationRequest(String token);
    boolean hasRegistrationVerification(String email);
    void terminateRegistration(String email);
    void confirmRegistration(ProcessedRegistrationRequest request);

    TwoFactorService.InitiationData initiateTwoFactorAuthentication(User user);
    void terminateTwoFactorInitiation(User user);
    boolean confirmTwoFactor(User user, String code);

    void changePassword(User user, String newPassword, String twoFactorCode);
    void logout(UserSession session);

    UserSession findSession(String sessionText);
    void updateIpAddress(UserSession session, String ipAddress);

    record ProcessedRegistrationRequest(String email, String hashedPassword, UserProfile profile, String emailConfirmationToken) {}
    record RegistrationRequest(String email, String password, UserProfile profile) {}
}

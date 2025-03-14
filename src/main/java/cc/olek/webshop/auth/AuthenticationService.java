package cc.olek.webshop.auth;

import cc.olek.webshop.user.UserSession;

public interface AuthenticationService {
    UserSession authenticate(String email, String password);
    void logout(UserSession session);
    void changePassword(String email, String newPassword);

    UserSession findSession(String sessionText);
    void updateIpAddress(UserSession session, String ipAddress);
}

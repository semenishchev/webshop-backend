package cc.olek.webshop.auth;

public interface AuthenticationService {
    UserSession authenticate(String username, String password);
    void logout(UserSession session);
}

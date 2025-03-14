package cc.olek.webshop.user;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class UserContext {
    private User user;
    private UserSession session;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public UserSession getSession() {
        return session;
    }
}

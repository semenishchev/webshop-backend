package cc.olek.webshop.service;

import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserServicePanache implements UserService {
    @Transactional
    @Override
    public User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.persist();
        Log.infof("Registered user %s", email);
        return user;
    }

    @Transactional
    @Override
    public User createUserRaw(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordRaw(rawPassword);
        user.persist();
        Log.infof("Registered user %s", email);
        return user;
    }

    @Override
    public User findUserById(long id) {
        return User.findById(id);
    }

    @Override
    public User findUserByEmail(String email) {
        return User.find("email", email).firstResult();
    }

    @Override
    @Transactional
    public void saveUser(User actionOn) {
        User.getEntityManager().merge(actionOn);
    }

    @Override
    public long getTotalUsers() {
        return User.count();
    }
}

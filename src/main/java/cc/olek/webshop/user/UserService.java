package cc.olek.webshop.user;

public interface UserService {
    User createUser(String email, String password);
    User findUserById(long id);
    User findUserByEmail(String email);
}

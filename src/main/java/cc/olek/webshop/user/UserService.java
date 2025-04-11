package cc.olek.webshop.user;

public interface UserService {
    User createUser(String email, String password);

    /**
     * Creates a user with already hashed password
     * @param email Email of the user
     * @param rawPassword Already hashed password
     * @return new registered user
     */
    User createUserRaw(String email, String rawPassword);
    User findUserById(long id);
    User findUserByEmail(String email);

    void saveUser(User actionOn);
    long getTotalUsers();
}

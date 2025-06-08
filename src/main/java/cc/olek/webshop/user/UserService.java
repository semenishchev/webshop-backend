package cc.olek.webshop.user;

import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.BadRequestException;

public interface UserService {
    User createUser(String email, String password);

    /**
     * Creates a user with already hashed password
     *
     * @param email       Email of the user
     * @param rawPassword Already hashed password
     * @param profile
     * @return new registered user
     */
    User createUserRaw(String email, String rawPassword, UserProfile profile);
    User findUserById(long id);
    User findUserByEmail(String email);

    void saveUser(User actionOn);
    long getTotalUsers();

    default User parse(User executor, String id) {
        if(id.equalsIgnoreCase("me")) {
            return executor;
        }

        if(!executor.isSuperuser()) {
            throw new UnauthorizedException();
        }

        try {
            return findUserById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID is not a number");
        }
    }
}

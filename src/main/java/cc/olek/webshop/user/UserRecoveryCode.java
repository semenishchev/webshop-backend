package cc.olek.webshop.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.mindrot.jbcrypt.BCrypt;

@Entity
public class UserRecoveryCode extends PanacheEntity {
    @ManyToOne
    public User user;
    private String hashedCode;

    public UserRecoveryCode() {}

    public UserRecoveryCode(User user, String code) {
        this.user = user;
        this.hashedCode = BCrypt.hashpw(code, BCrypt.gensalt());
    }

    public boolean validate(String code) {
        return BCrypt.checkpw(code, hashedCode);
    }
}

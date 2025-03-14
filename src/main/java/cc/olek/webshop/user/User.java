package cc.olek.webshop.user;

import cc.olek.webshop.entity.WebshopEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Users", indexes = { @Index(unique = true, columnList = "email") })
public class User extends WebshopEntity {
    private String email;
    @Embedded
    private UserProfile profile;

    @JsonIgnore
    private String hashedPassword;

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.hashedPassword);
    }

    public void setPassword(String password) {
        this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public UserProfile getProfile() {
        if(this.profile == null) {
            return this.profile = new UserProfile();
        }
        return profile;
    }
}

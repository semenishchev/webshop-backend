package cc.olek.webshop.user;

import cc.olek.webshop.entity.WebshopEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "RegisteredUser", indexes = { @Index(unique = true, columnList = "email") }) // RegisteredUser because User is a postgres keyword
public class User extends WebshopEntity {
    @Column(length = 50)
    private String email;

    @Embedded
    private UserProfile profile;

    @ElementCollection
    private Map<Long, Integer> cart;

    @JsonIgnore
    private String hashedPassword;

    @JsonIgnore
    @Column(length = 128)
    private String twoFactorSecret;

    private boolean isSuperuser = false;

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.hashedPassword);
    }

    public void setPassword(String password) {
        this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void setPasswordRaw(String password) {
        this.hashedPassword = password;
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

    public void setSuperuser(boolean flag) {
        this.isSuperuser = true;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public boolean isSuperuser() {
        return this.isSuperuser;
    }

    public Map<Long, Integer> getCart() {
        return cart;
    }
}

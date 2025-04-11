package cc.olek.webshop.user;

import cc.olek.webshop.entity.WebshopEntity;
import cc.olek.webshop.shop.model.Cart;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "RegisteredUser", indexes = { @Index(unique = true, columnList = "email") }) // RegisteredUser because User is a postgres keyword
public class User extends WebshopEntity {
    @Column(length = 50)
    private String email;

    @Embedded
    private UserProfile profile;

    @Embedded
    private Cart cart;

    @JsonIgnore
    private String hashedPassword;

    @JsonIgnore
    @Column(length = 128)
    private String twoFactorSecret;

    private boolean isSuperuser = false;

    @ElementCollection
    @Enumerated(EnumType.ORDINAL)
    @CollectionTable(name = "UserPermission", joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

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

    public Cart getCart() {
        if(this.cart == null) {
            return this.cart = new Cart();
        }
        return cart;
    }

    public boolean hasPermission(Permission permission) {
        if(this.isSuperuser) return true;
        return this.permissions.contains(permission);
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
}

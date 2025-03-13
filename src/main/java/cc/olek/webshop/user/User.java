package cc.olek.webshop.user;

import cc.olek.webshop.auth.UserSession;
import cc.olek.webshop.entity.WebshopEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Users")
public class User extends WebshopEntity {
    public String email;
    public String fullName;

    @JsonIgnore
    public String hashedPassword;

    @OneToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @JsonIgnore
    public List<UserSession> userSessions;

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.hashedPassword);
    }

    public void setPassword(String password) {
        this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }
}

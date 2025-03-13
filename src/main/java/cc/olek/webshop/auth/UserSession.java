package cc.olek.webshop.auth;

import cc.olek.webshop.user.User;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class UserSession extends PanacheEntity {
    public String sessionText;
    @ManyToOne
    public User user;
    public Date expiresAt;
    public String ipAddress;

    public boolean hasExpired() {
        return expiresAt.before(new Date());
    }
}

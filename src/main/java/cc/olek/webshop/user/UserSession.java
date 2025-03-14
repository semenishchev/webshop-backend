package cc.olek.webshop.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.*;

import java.util.Date;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserSession extends PanacheEntity {
    public String sessionText;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User user;
    public Date expiresAt;
    public String ipAddress;

    public boolean hasExpired() {
        return expiresAt.before(new Date());
    }
}

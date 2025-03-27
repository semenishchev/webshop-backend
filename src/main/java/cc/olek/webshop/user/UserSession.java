package cc.olek.webshop.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.util.Date;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(indexes = @Index(name = "sessionIndex", columnList = "sessionText", unique = true))
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

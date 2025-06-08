package cc.olek.webshop.entity;

import cc.olek.webshop.Webshop;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.MappedSuperclass;

public abstract class WebshopEntityBase extends PanacheEntityBase {
    @Override
    public String toString() {
        try {
            return Webshop.JSON.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

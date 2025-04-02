package cc.olek.webshop.entity;

import cc.olek.webshop.Webshop;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

public abstract class WebshopEntity extends PanacheEntity {
    @Override
    public String toString() {
        try {
            return Webshop.JSON.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

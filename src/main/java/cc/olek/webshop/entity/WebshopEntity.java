package cc.olek.webshop.entity;

import cc.olek.webshop.ConstantPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

public abstract class WebshopEntity extends PanacheEntity {
    @Override
    public String toString() {
        try {
            return ConstantPool.JSON.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

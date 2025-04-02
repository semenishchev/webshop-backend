package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
public class DeliveryStatus {
    public String deliveryProvider;
    public String deliveryReferenceId;
    public String traceNumber;
    public String traceLink;

    @OneToMany
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    public List<DeliveryUpdate> updates = new ArrayList<>();

    @Entity
    public static class DeliveryUpdate extends WebshopEntity {
        public String message;
        public Date whenHappened;

        public DeliveryUpdate() {}

        public DeliveryUpdate(String message) {
            this.message = message;
            this.whenHappened = new Date();
        }
    }
}

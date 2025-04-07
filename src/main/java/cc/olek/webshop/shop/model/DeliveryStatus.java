package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
public class DeliveryStatus {
    @Column(length = 10)
    public String deliveryProvider;
    @Column(length = 50)
    public String deliveryReferenceId;
    @Column(length = 50)
    public String traceNumber;
    public String traceLink;

    @OneToMany
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "deliveryUpdates")
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

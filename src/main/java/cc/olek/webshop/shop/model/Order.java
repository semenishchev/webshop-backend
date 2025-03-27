package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import cc.olek.webshop.user.User;
import jakarta.json.JsonObject;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Order extends WebshopEntity {
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User customer;

    public OrderStatus status = OrderStatus.CREATED;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    public DeliveryAddress deliveryAddress;

    public int paidPrice;
    public Currency currency;

    @ElementCollection
    @CollectionTable(name = "orderProducts", joinColumns = @JoinColumn(name = "orderId"))
    @MapKeyJoinColumn(name = "productId")
    @Column(name = "quantity")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Product, Integer> products = new HashMap<>();

    public enum OrderStatus {
        CREATED,
        AWAITING_PAYMENT,
        PAID,
        IN_DELIVERY,
        RETURN_INITIATED,
        REFUNDED,
        REJECTED,
        DONE
    }
}

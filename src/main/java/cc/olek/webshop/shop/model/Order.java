package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import cc.olek.webshop.entity.WebshopEntityBase;
import cc.olek.webshop.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.id.UUIDGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "Orders", indexes = @Index(name = "paymentIdIndex", columnList = "paymentId"))
public class Order extends WebshopEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User customer;

    @Enumerated(EnumType.STRING)
    public Status status = Status.CREATED;

    @Embedded
    public DeliveryAddress deliveryAddress;

    @Embedded
    public DeliveryStatus deliveryStatus;

    public String paymentId;

    public int paidPrice;

    @ElementCollection
    @CollectionTable(name = "orderProducts", joinColumns = @JoinColumn(name = "orderId"))
    @MapKeyJoinColumn(name = "productId")
    @Column(name = "quantity")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Map<Product, Integer> products = new HashMap<>();

    public enum Status {
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

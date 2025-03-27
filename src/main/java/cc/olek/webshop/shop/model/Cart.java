package cc.olek.webshop.shop.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashMap;
import java.util.Map;

@Embeddable
public class Cart {
    @ElementCollection
    @CollectionTable(name = "cartData", joinColumns = @JoinColumn(name = "cartId"))
    @MapKeyJoinColumn(name = "productId")
    @Column(name = "quantity")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Product, Integer> products = new HashMap<>();
}

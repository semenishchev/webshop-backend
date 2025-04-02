package cc.olek.webshop.shop.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Embeddable
public class Cart {
    @ElementCollection
    @CollectionTable(name = "cartData", joinColumns = @JoinColumn(name = "cartId"))
    @MapKeyJoinColumn(name = "productId")
    @Column(name = "quantity")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Map<Product, Integer> products = new HashMap<>();

    public Map<Product, Integer> getProducts() {
        return products;
    }

    public CartInfo calculateInfo() {
        int price = 0;
        StringBuilder infoBuilder = new StringBuilder();
        for (Iterator<Map.Entry<Product, Integer>> iterator = products.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Product, Integer> entry = iterator.next();
            Product product = entry.getKey();
            int amount = entry.getValue();
            price += amount * product.basePrice;
            infoBuilder.append(amount).append("x ").append(product.name);
            if(iterator.hasNext()) {
                infoBuilder.append("\n");
            }
        }
        return new CartInfo(price, infoBuilder.toString());
    }

    public record CartInfo(int totalPrice, String description) {}
}

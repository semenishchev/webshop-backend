package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Entity
public class Review extends WebshopEntity {
    @ManyToOne
    public Product product;
    public String title;
    public String content;
    public int rating;

    @PostPersist
    @PostUpdate
    @Transactional
    public void updateRating() {
        product.calculateAverageRating();
    }
}

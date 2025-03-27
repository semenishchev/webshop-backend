package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Review extends WebshopEntity {
    public String title;
    public String content;
    public int rating;
}

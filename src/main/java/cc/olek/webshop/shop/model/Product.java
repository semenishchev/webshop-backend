package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
public class Product extends WebshopEntity {
    public String name;

    @ManyToOne
    public ProductCategory category;

    public int basePrice; // in euros

    @Transient
    private int averageRating = 0;

    public int getAverageRating() {
        return averageRating;
    }
}

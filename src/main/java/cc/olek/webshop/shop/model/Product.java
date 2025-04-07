package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
public class Product extends WebshopEntity {
    @Column(length = 50)
    public String name;
    
    @Column(columnDefinition = "text")
    public String description;

    @ManyToOne
    public ProductCategory category;

    public int price; // in euros

    public int timesBought = 0;
    public int stock = 0;

    @Transient
    private int averageRating = 0;

    public int getAverageRating() {
        return averageRating;
    }
}

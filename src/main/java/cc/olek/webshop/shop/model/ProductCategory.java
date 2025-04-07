package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class ProductCategory extends WebshopEntity {
    @Column(length = 50)
    public String name;
    public String iconLink;
}

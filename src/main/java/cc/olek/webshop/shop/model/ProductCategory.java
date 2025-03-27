package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import jakarta.persistence.Entity;

@Entity
public class ProductCategory extends WebshopEntity {
    public String name;
    public String iconLink;
}

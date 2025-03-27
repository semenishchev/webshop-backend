package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import cc.olek.webshop.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.Locale;

@Entity
public class DeliveryAddress extends WebshopEntity {
    @ManyToOne
    public User owner;
    public Locale.IsoCountryCode country;
    public String street;
    public String city;
    public String state;
    public String zip;
}

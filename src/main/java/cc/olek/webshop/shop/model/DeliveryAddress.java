package cc.olek.webshop.shop.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record DeliveryAddress(String country, String street, String city, int house, String state, String zip) {
}

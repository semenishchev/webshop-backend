package cc.olek.webshop.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class WebshopEntity extends WebshopEntityBase {
    @Id
    @GeneratedValue
    public Long id;
}

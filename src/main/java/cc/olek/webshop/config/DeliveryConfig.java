package cc.olek.webshop.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "origin-delivery")
public interface DeliveryConfig {
    String accountNumber();
    String country();
    String street();
    String house();
    String city();
    String state();
    String zip();
}

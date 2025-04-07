package cc.olek.webshop.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "stripe")
public interface StripeConfig {
    String apiKey();
    String apiSecret();
    String returnUrl();
}

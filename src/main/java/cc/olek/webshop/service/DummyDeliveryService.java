package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.DeliveryStatus;
import cc.olek.webshop.shop.service.DeliveryService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class DummyDeliveryService implements DeliveryService {

    @Override
    public int calculateDeliveryPrice(DeliveryAddress address, int weight, int height, int width, int length) {
        return ThreadLocalRandom.current().nextInt(1, weight + 1);
    }

    @Override
    public int calculateDeliveryDuration(DeliveryAddress address) {
        return 2;
    }

    @Override
    public DeliveryStatus askForDelivery(DeliveryAddress address) {
        DeliveryStatus result = new DeliveryStatus();
        result.deliveryProvider = "DHL";
        result.traceLink = "https://www.youtube.com/watch?v=xMHJGd3wwZk";
        result.traceNumber = UUID.randomUUID().toString();
        result.updates = new ArrayList<>();
        return result;
    }
}

package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.DeliveryStatus;

public interface DeliveryService {

    /**
     * Calculates delivery price in EUR according to the following parameters
     * @param address Address of the receiver
     * @param weight in grams
     * @param height in cm
     * @param width in cm
     * @param length in cm
     * @return price in EUR to deliver
     */
    int calculateDeliveryPrice(DeliveryAddress address, int weight, int height, int width, int length);
    int calculateDeliveryDuration(DeliveryAddress address);
    default DeliveryInfo calculateDelivery(DeliveryAddress address, int weight, int height, int width, int length) {
        return new DeliveryInfo(calculateDeliveryPrice(address, weight, height, width, length), calculateDeliveryDuration(address));
    }

    /**
     * Brings out the initial delivery status
     * @param address Address to deliver
     * @return Fresh status
     */
    DeliveryStatus askForDelivery(DeliveryAddress address);

    record DeliveryInfo(int deliveryPrice, int deliveryDuration) {}
}

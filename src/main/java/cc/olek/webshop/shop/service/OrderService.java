package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.Cart;
import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.Order;

import java.net.URL;

public interface OrderService {
    /**
     * Places order from the user cart
     * @param cart - Cart from which an order should be created
     * @param address - Where order should be delivered
     * @return OrderPlacement which contains URL where user should be redirected for payment and an Order object.
     */
    OrderPlacement placeOrder(Cart cart, DeliveryAddress address);
    Order findOrderByPaymentId(String paymentId);
    void updateOrder(Order order);

    void markAsPaid(Order order);

    void cancelOrder(Order relativeOrder);

    record OrderPlacement(String paymentUrl, String invoiceId) {}
}

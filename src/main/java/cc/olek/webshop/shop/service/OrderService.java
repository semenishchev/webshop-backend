package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.user.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderService {
    /**
     * Places order from the user cart
     * @param cart - Cart from which an order should be created
     * @param address - Where order should be delivered
     * @return OrderPlacement which contains URL where user should be redirected for payment and an Order object.
     */
    OrderPlacement placeOrder(User user, Map<Long, Integer> cart, DeliveryAddress address);
    Order findOrderByPaymentId(String paymentId);
    Order findOrderById(UUID orderId);

    void updateOrder(Order order);

    void markAsPaid(Order order);

    void cancelOrder(Order relativeOrder);

    List<Order> findOrdersOfUser(User target);

    record OrderPlacement(String paymentUrl, String invoiceId, Order order) {}
}

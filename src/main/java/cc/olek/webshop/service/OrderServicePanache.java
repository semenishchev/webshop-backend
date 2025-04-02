package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.Cart;
import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.DeliveryStatus;
import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.shop.service.DeliveryService;
import cc.olek.webshop.shop.service.OrderService;
import cc.olek.webshop.shop.service.PaymentService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.util.TransactionHelper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class OrderServicePanache implements OrderService {
    @Inject
    UserContext userContext;

    @Inject
    DeliveryService deliveryService;

    @Inject
    PaymentService paymentProcessor;

    @Inject
    TransactionHelper transactionHelper;

    @Override
    @Transactional
    public OrderPlacement placeOrder(Cart cart, DeliveryAddress address) {
        User user = userContext.getUser();
        Order order = new Order();
        order.customer = user;
        order.status = Order.Status.CREATED;
        order.deliveryAddress = address;
        order.products = new HashMap<>(cart.getProducts());
        DeliveryService.DeliveryInfo deliveryInfo = deliveryService.calculateDelivery(
            address,
            1000,
            40,
            50,
            30
        );
        order.persist();
        OrderPlacement payment = paymentProcessor.createPayment(order, deliveryInfo);
        transactionHelper.registerRollbackAction(() -> paymentProcessor.cancelSession(payment.invoiceId()));
        order.paymentId = payment.invoiceId();
        order.persist();
        return payment;
    }

    @Override
    public Order findOrderByPaymentId(String paymentId) {
        return Order.find("paymentId", paymentId).firstResult();
    }

    @Override
    @Transactional
    public void updateOrder(Order order) {
        Order.getEntityManager().merge(order);
    }

    @Override
    public void markAsPaid(Order order) {
        order.status = Order.Status.PAID;
        DeliveryStatus deliveryStatus;
        try {
            deliveryStatus = deliveryService.askForDelivery(order.deliveryAddress);
        } catch (Exception e) {
            deliveryStatus = new DeliveryStatus();
            deliveryStatus.deliveryReferenceId = "ERROR";
            Log.errorf(e, "An error occurred when asking for delivery for %d", order.id);
        }
        deliveryStatus.updates = List.of(new DeliveryStatus.DeliveryUpdate("Order placed"));
        order.deliveryStatus = deliveryStatus;
        updateOrder(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Order relativeOrder) {
        String invoiceId = relativeOrder.paymentId;
        paymentProcessor.cancelSession(invoiceId);
    }
}

package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.*;
import cc.olek.webshop.shop.service.DeliveryService;
import cc.olek.webshop.shop.service.OrderService;
import cc.olek.webshop.shop.service.PaymentService;
import cc.olek.webshop.shop.service.ProductService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.util.TransactionHelper;
import com.stripe.param.RefundCreateParams;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class OrderServicePanache implements OrderService {

    @Inject
    DeliveryService deliveryService;

    @Inject
    PaymentService paymentProcessor;

    @Inject
    ProductService productService;

    @Inject
    TransactionHelper transactionHelper;

    @Override
    @Transactional
    public OrderPlacement placeOrder(User user, Map<Long, Integer> cart, DeliveryAddress address) {
        Order order = new Order();
        order.customer = user;
        order.status = Order.Status.CREATED;
        order.deliveryAddress = address;
        Map<Product, Integer> products = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            products.put(productService.getProductById(entry.getKey()), entry.getValue());
        }
        order.products = products;
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
    public Order findOrderById(UUID orderId) {
        return Order.findById(orderId);
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
    public void cancelOrder(Order order) {
        String invoiceId = order.paymentId;
        Order.Status status = order.status;
        if(status == null) {
            throw new NotFoundException();
        }
        if(status == Order.Status.PAID) {
            paymentProcessor.refund(order.paymentId, RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER, 100);
            return;
        }
        if(status.ordinal() >= Order.Status.PAID.ordinal()) {
            throw new BadRequestException();
        }
        paymentProcessor.cancelSession(invoiceId);
    }

    @Override
    public List<Order> findOrdersOfUser(User target) {
        return Order.find("customer", target).firstResult();
    }
}

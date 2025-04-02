package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.shop.model.Product;
import com.stripe.param.RefundCreateParams;

import java.util.Map;

public interface PaymentService {
    OrderService.OrderPlacement createPayment(Order order, DeliveryService.DeliveryInfo deliveryInfo);

    void cancelSession(String paymentId);

    /**
     * Refunds a transaction
     * @param paymentId Invoice reference
     * @param percentage range from >0 to 1. 0 should throw an exception
     */
    void refund(String paymentId, RefundCreateParams.Reason reason, int percentage);
    void refundAmount(String paymentId, RefundCreateParams.Reason reason, int amount);
}

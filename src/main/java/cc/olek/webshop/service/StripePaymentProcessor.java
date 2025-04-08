package cc.olek.webshop.service;

import cc.olek.webshop.config.StripeConfig;
import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.service.DeliveryService;
import cc.olek.webshop.shop.service.OrderService;
import cc.olek.webshop.shop.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class StripePaymentProcessor implements PaymentService {

    @Inject
    StripeConfig config;

    // fucking stripe with its builders
    @Override
    public OrderService.OrderPlacement createPayment(Order order, DeliveryService.DeliveryInfo deliveryInfo) {
        SessionCreateParams.Builder params = SessionCreateParams.builder();
        params.setSubmitType(SessionCreateParams.SubmitType.PAY);
        params.setMode(SessionCreateParams.Mode.PAYMENT);
        params.setClientReferenceId(order.customer.id.toString());
        params.setCustomerEmail(order.customer.getEmail());
        params.setReturnUrl(config.returnUrl());
        params.putMetadata("orderId", String.valueOf(order.id));
        for (Map.Entry<Product, Integer> entry : order.products.entrySet()) {
            var productEntryBuilder = SessionCreateParams.LineItem.builder()
                .setPriceData(SessionCreateParams.LineItem.PriceData
                    .builder()
                    .setProduct(entry.getKey().name)
                    .setUnitAmount((long) entry.getKey().price)
                    .setCurrency("EUR")
                    .build()
                )
                .setQuantity(entry.getValue().longValue())
                .setAdjustableQuantity(SessionCreateParams.LineItem.AdjustableQuantity
                    .builder()
                    .setEnabled(false)
                    .build()
                );
            params.addLineItem(productEntryBuilder.build());
        }
        params.addShippingOption(SessionCreateParams.ShippingOption.builder()
            .setShippingRateData(SessionCreateParams.ShippingOption.ShippingRateData
                .builder()
                .setDisplayName("DHL")
                .setDeliveryEstimate(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.builder()
                    .setMinimum(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum
                        .builder()
                        .setUnit(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY)
                        .setValue((long)deliveryInfo.deliveryDuration())
                        .build()
                    )
                    .build()
                )
                .setFixedAmount(SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount
                    .builder()
                    .setAmount((long)deliveryInfo.deliveryPrice())
                    .setCurrency("EUR")
                    .build()
                )
                .build()
            )
            .build()
        );
        Session session;
        try {
            session = Session.create(params.build());
        } catch (StripeException e) {
            Log.errorf(e, "Failed to create session for order id: %i user: %i %s", order.id, order.customer.id, order.customer.getEmail());
            throw new RuntimeException(e);
        }
        return new OrderService.OrderPlacement(session.getUrl(), session.getId());
    }

    @Override
    public void cancelSession(String paymentId) {
        Session session;
        try {
            session = Session.retrieve(paymentId);
        } catch (StripeException e) {
            Log.errorf(e, "Failed to retrieve session by id %s", paymentId);
            return;
        }
        if(session == null) return;
        if(session.getStatus().equalsIgnoreCase("paid")) {
            throw new RuntimeException("Session is already paid");
        }
        try {
            session.expire();
        } catch (StripeException e) {
            Log.errorf(e, "Failed to cancel session by id: %s", paymentId);
        }
    }

    @Override
    public void refund(String paymentId, RefundCreateParams.Reason reason, int percentage) {
        Session session;
        session = fetchSession(paymentId);
        if (session == null) return;

        RefundCreateParams.Builder builder = RefundCreateParams.builder();
        builder.setPaymentIntent(session.getPaymentIntent());
        builder.setAmount(session.getAmountSubtotal() * percentage);
        builder.setReverseTransfer(true);
        builder.setReason(reason);
        try {
            Refund.create(builder.build());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refundAmount(String paymentId, RefundCreateParams.Reason reason, int amount) {
        Session session = fetchSession(paymentId);
        if (session == null) return;
        RefundCreateParams.Builder builder = RefundCreateParams.builder();
        builder.setPaymentIntent(session.getPaymentIntent());
        builder.setAmount((long)amount);
        builder.setReverseTransfer(true);
        builder.setReason(reason);
        try {
            Refund.create(builder.build());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private static Session fetchSession(String paymentId) {
        Session session;
        try {
            session = Session.retrieve(paymentId);
        } catch (StripeException e) {
            Log.errorf(e, "Failed to fetch session by id: %s", paymentId);
            return null;
        }
        if (session == null) return null;
        if (!session.getStatus().equalsIgnoreCase("paid")) {
            throw new IllegalStateException("Session is not paid");
        }
        return session;
    }
}

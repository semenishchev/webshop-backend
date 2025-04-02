package cc.olek.webshop.shop.resource;

import cc.olek.webshop.config.StripeConfig;
import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.shop.service.OrderService;
import cc.olek.webshop.user.UserContext;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;

@Path("/payment")
public class PaymentResource {

    @Inject
    StripeConfig config;

    @Inject
    OrderService orderService;

    @Inject
    UserContext user;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/stripe")
    public String stripeWebhook(String data, @HeaderParam("Stripe-Signature") String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(data, signature, config.apiSecret());
        } catch (SignatureVerificationException e) {
            throw new UnauthorizedException();
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject == null) {
            throw new BadRequestException();
        }
        if(!(stripeObject instanceof PaymentIntent payment)) return "unhandled";
        String paymentId = payment.getId();
        Order relativeOrder = orderService.findOrderByPaymentId(paymentId);
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                orderService.markAsPaid(relativeOrder);
            }
            case "payment_intent.cancelled" -> {
                orderService.cancelOrder(relativeOrder);
            }
        }
        return "ok";
    }

    @Authenticated
    @GET
    @Path("/status/{id}")
    public Order.Status getStatus(@PathParam("id") String id) {
        Order order = orderService.findOrderByPaymentId(id);
        if(order == null) throw new NotFoundException();
        if(!Objects.equals(order.customer.id, user.getUser().id)) throw new NotFoundException();
        return order.status;
    }
}

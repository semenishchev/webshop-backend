package cc.olek.webshop.shop.resource;

import cc.olek.webshop.shop.model.DeliveryAddress;
import cc.olek.webshop.shop.model.Order;
import cc.olek.webshop.shop.service.OrderService;
import cc.olek.webshop.user.User;
import cc.olek.webshop.user.UserContext;
import cc.olek.webshop.user.UserService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.*;

@Path("/order")
@Authenticated
public class OrderResource {

    @Inject
    UserContext userContext;

    @Inject
    OrderService orderService;

    @Inject
    UserService userService;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Order> getOrders(@QueryParam("user") String user) {
        User target = userService.parse(userContext.getUser(), user);
        if(target == null) {
            throw new NotFoundException();
        }
        return orderService.findOrdersOfUser(target);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{orderId}")
    public Order getOrder(@PathParam("orderId") UUID orderId) {
        Order order = orderService.findOrderById(orderId);
        if(order == null) {
            throw new NotFoundException();
        }
        if(!order.customer.id.equals(userContext.getUser().id)) {
            throw new UnauthorizedException();
        }
        return order;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/new")
    public OrderService.OrderPlacement placeOrder(OrderRequest request) {
        List<ProductOrder> products = request.products();
        if(products.size() > 50) {
            throw new BadRequestException("Too much products");
        }

        Map<Long, Integer> map = new HashMap<>();
        for (ProductOrder product : products) {
            map.put(product.productId, product.amount);
        }
        return orderService.placeOrder(userContext.getUser(), map, request.address());
    }

    public record ProductOrder(long productId, int amount) {}
    public record OrderRequest(DeliveryAddress address, List<ProductOrder> products) {}
}

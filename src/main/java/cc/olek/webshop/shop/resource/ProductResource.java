package cc.olek.webshop.shop.resource;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/products")
public class ProductResource {

    private static final int PRODUCTS_PER_PAGE = 10;

    @Inject
    ProductService productService;

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> searchProducts(
        @QueryParam("category") String category,
        @QueryParam("sorting") ProductSorting sorting,
        @QueryParam("page") int page,
        @QueryParam("hint") String queryHint
    ) {
        if(sorting == null) {
            sorting = ProductSorting.RELEVANCE;
        }

        productService.getProductsByCategory(productService.getCategory(category), sorting, PRODUCTS_PER_PAGE * page, PRODUCTS_PER_PAGE);
        return List.of();
    }

    @GET
    @Path("/get")
    public Product getProduct(@QueryParam("id") int id) {
        if(id <= 0) {
            throw new NotFoundException();
        }

        return productService.getProductById(id);
    }
}

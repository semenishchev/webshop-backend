package cc.olek.webshop.shop.resource;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/products")
public class ProductResource {

    private static final int PRODUCTS_PER_PAGE = 10;

    @Inject
    ProductService productService;

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts(
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
}

package cc.olek.webshop.shop.resource;

import cc.olek.webshop.auth.AdminRequired;
import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
        @QueryParam("query") String queryHint
    ) {
        ProductCategory catObject = productService.getCategory(category);
        int begin = PRODUCTS_PER_PAGE * page;
        if(queryHint == null) {
            return productService.getProductsByCategory(catObject, sorting, begin, PRODUCTS_PER_PAGE);
        }
        return productService.search(catObject, sorting, begin, PRODUCTS_PER_PAGE, queryHint);
    }

    @GET
    @Path("/search-autocomplete")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> autocomplete(@QueryParam("query") String query) {
        return productService.autocomplete(query);
    }

    @GET
    @Path("/get")
    public Product getProduct(@QueryParam("id") int id) {
        if(id <= 0) {
            throw new NotFoundException();
        }

        return productService.getProductById(id);
    }

    @AdminRequired
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/update")
    public Response updateProduct(@QueryParam("id") int id, Product newData) {
        productService.updateProduct(id, newData);
        return Response.status(Response.Status.OK).entity("ok").build();
    }

    @AdminRequired
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/new")
    public Response newProduct(Product newProduct) {
        productService.registerNewProduct(newProduct);
        return Response.status(Response.Status.CREATED).entity("ok").build();
    }

    @AdminRequired
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/new")
    public Response deleteProduct(int id) {
        productService.deleteProduct(id);
        return Response.status(Response.Status.OK).entity("ok").build();
    }
}

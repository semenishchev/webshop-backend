package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.lang.reflect.Method;
import java.util.List;

@ApplicationScoped
public class ProductServicePanache implements ProductService {
    @Override
    public ProductCategory getCategory(String categoryName) {
        return (ProductCategory) ProductCategory.find("name", categoryName)
            .firstResultOptional()
            .orElseThrow(NotFoundException::new);
    }

    @Override
    @CacheResult(cacheName = "productReviews", keyGenerator = ProductKeyGenerator.class)
    public int calculateAverageRating(Product product) {
        return 0;
    }

    @Override
    public List<Product> getProductsByCategory(ProductCategory category, ProductSorting sorting, int begin, int limit) {
        return List.of();
    }

    @Override
    public void registerNewProduct(Product product) {

    }

    @Override
    public void updateProduct(Product product) {

    }

    @Override
    public Product getProductById(int id) {
        return (Product) Product.findByIdOptional(id)
            .orElseThrow(NotFoundException::new);
    }

    public static class ProductKeyGenerator implements CacheKeyGenerator {
        @Override
        public Object generate(Method method, Object... methodParams) {
            if(methodParams.length == 0) return method.getName();
            Object first = methodParams[0];
            if(!(first instanceof Product product)) return first.toString();
            return product.id;
        }
    }
}

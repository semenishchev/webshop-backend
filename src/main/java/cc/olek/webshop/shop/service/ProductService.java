package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CacheResult;

import java.lang.reflect.Method;
import java.util.List;

public interface ProductService {
    ProductCategory getCategory(String categoryName);

    @CacheResult(cacheName = "productRatings", keyGenerator = ProductKeyGenerator.class)
    List<Product> getProductsByCategory(ProductCategory category, ProductSorting sorting, int begin, int limit);

    @CacheResult(cacheName = "autocompletions")
    List<String> autocomplete(String query);

    List<Product> search(ProductCategory category, ProductSorting sorting, int begin, int limit, String query);
    void registerNewProduct(Product product);
    void updateProduct(long id, Product product);

    Product getProductById(long id);

    void deleteProduct(long id);

    void registerCategory(ProductCategory category);

    class ProductKeyGenerator implements CacheKeyGenerator {
        @Override
        public Object generate(Method method, Object... methodParams) {
            if(methodParams.length == 0) return method.getName();
            Object first = methodParams[0];
            if(!(first instanceof Product product)) return first.toString();
            return product.id;
        }
    }
}

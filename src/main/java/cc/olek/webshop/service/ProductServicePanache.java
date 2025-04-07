package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductServicePanache implements ProductService {
    @Override
    public ProductCategory getCategory(String categoryName) {
        return null;
    }

    @Override
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
}

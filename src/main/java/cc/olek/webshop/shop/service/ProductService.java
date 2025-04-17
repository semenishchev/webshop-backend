package cc.olek.webshop.shop.service;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;

import java.util.List;

public interface ProductService {
    ProductCategory getCategory(String categoryName);

    int calculateAverageRating(Product product);
    List<Product> getProductsByCategory(ProductCategory category, ProductSorting sorting, int begin, int limit);
    void registerNewProduct(Product product);
    void updateProduct(Product product);

    Product getProductById(int id);
}

package cc.olek.webshop.service;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.model.Review;
import cc.olek.webshop.shop.service.ProductService;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.MatchPredicateOptionsStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;

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
    @Transactional
    public void registerCategory(ProductCategory category) {
        category.persistAndFlush();
    }

    @Override
    public List<Product> getProductsByCategory(ProductCategory category, ProductSorting sorting, int begin, int limit) {
        return Product.find("category", category)
            .range(begin, begin + limit)
            .list();
    }

    @Override
    public List<String> autocomplete(String query) {
        var fetch = Search.session(Product.getSession())
            .search(Product.class)
            .where(builder -> builder.match()
                .fields("productName", "category")
                .matching(query)
                .analyzer("autocomplete")
                .fuzzy(2)
            )
            .fetch(5);
        return fetch.hits()
            .stream()
            .map(p -> p.name)
            .distinct()
            .toList();
    }

    @Override
    public List<Product> search(ProductCategory category, ProductSorting sorting, int begin, int limit, String query) {
        return search(category, sorting, begin, limit, query, null);
    }

    public List<Product> search(ProductCategory category, ProductSorting sorting, int begin, int limit, String queryHint, String analyzer) {
        if(sorting == null) {
            sorting = ProductSorting.RELEVANCE;
        }
        SearchQueryOptionsStep<?, Product, SearchLoadingOptionsStep, ?, ?> query = Search.session(Product.getSession())
            .search(Product.class)
            .where(builder -> {
                BooleanPredicateClausesStep<?> predicate = builder.bool();
                if (category != null) {
                    predicate = predicate.filter(builder.match()
                        .field("category")
                        .matching(category)
                    );
                }
                MatchPredicateOptionsStep<?> matcher = builder
                    .match()
                    .fields("productName", "description", "category")
                    .matching(queryHint)
                    .fuzzy(2);
                if (analyzer != null) {
                    matcher = matcher.analyzer(analyzer);
                }
                return predicate.should(matcher);
            });
        switch (sorting) {
            case REVIEW -> {
                query = query.sort(f -> f.field("averageRating").desc());
            }
            case TIMES_BOUGHT -> {
                query = query.sort(f -> f.field("timesBought").desc());
            }
            case RELEVANCE -> {
                query = query.sort(f -> f.composite()
                    .add(f.score())
                    .add(f.field("weighedRelevance").desc()));
            }

            case PRICE -> {
                query = query.sort(f -> f.field("price").desc());
            }
        }
        SearchResult<Product> fetch;
        if(begin != -1) {
            fetch = query.fetch(begin, limit);
        } else {
            fetch = query.fetch(limit);
        }
        Log.info("Search took " + fetch.took());
        return fetch.hits();
    }

    @Override
    @Transactional
    public void registerNewProduct(Product product) {
        product.persistAndFlush();
    }

    @Override
    @Transactional
    public void updateProduct(long id, Product product) {
        Product productById = getProductById(id);
        if(productById == null) throw new NotFoundException();
        mergeProducts(productById, product);
        Product.getEntityManager().merge(productById);
    }

    @Override
    public Product getProductById(long id) {
        return (Product) Product.findByIdOptional(id)
            .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public void deleteProduct(long id) {
        Product.deleteById(id);
    }

    private static void mergeProducts(Product target, Product source) {
        if(source == null) return;
        if(source.name != null) {
            target.name = source.name;
        }

        if(source.description != null) {
            target.description = source.description;
        }

        if(source.category != null) {
            target.category = source.category;
        }

        if(source.price != target.price) {
            target.price = source.price;
        }

        if(source.stock != target.stock) {
            target.stock = source.stock;
        }
    }
}

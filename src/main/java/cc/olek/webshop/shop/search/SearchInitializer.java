package cc.olek.webshop.shop.search;

import cc.olek.webshop.shop.model.Product;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.hibernate.search.mapper.orm.Search;

@ApplicationScoped
public class SearchInitializer {

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        Search.session(Product.getEntityManager()).schemaManager().createIfMissing();
    }
}

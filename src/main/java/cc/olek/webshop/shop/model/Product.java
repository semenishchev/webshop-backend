package cc.olek.webshop.shop.model;

import cc.olek.webshop.entity.WebshopEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

@Entity
@Indexed
public class Product extends WebshopEntity {
    @Column(length = 50)
    @FullTextField(analyzer = "title", name = "productName")
    @KeywordField(normalizer = "sort", name = "productNameKeyword")
    public String name;
    
    @Column(columnDefinition = "text")
    @FullTextField(analyzer = "description")
    public String description;

    @Column(length = 256)
    public String iconUrl;

    @ManyToOne
    @JsonSerialize(as = String.class)
    @JsonIgnore
    public ProductCategory category;

    @GenericField(sortable = Sortable.YES)
    public int price; // in cents

    @GenericField(sortable = Sortable.YES)
    public int timesBought = 0;

    public int stock = 0;

    @GenericField(sortable = Sortable.YES)
    public float averageRating = 0;

    @Transient
    @GenericField(sortable = Sortable.YES)
    @IndexingDependency(
        reindexOnUpdate = ReindexOnUpdate.SHALLOW,
        derivedFrom = {
            @ObjectPath(@PropertyValue(propertyName = "averageRating")),
            @ObjectPath(@PropertyValue(propertyName = "timesBought")),
            @ObjectPath(@PropertyValue(propertyName = "price"))
        }
    )
    public double getWeighedRelevance() {
        return averageRating + (1 - (100f / timesBought)) - (price / 100f);
    }

    @Transient
    @GenericField(name = "category")
    @IndexingDependency(derivedFrom = @ObjectPath(@PropertyValue(propertyName = "category")))
    public String getCategoryIndex() {
        return category.name;
    }

    @Transient
    public void calculateAverageRating() {
        Double val = Review.find("select avg(r.rating) from Review r where r.product = ?1", this)
            .project(Double.class)
            .firstResult();
        if(val == null) return;
        this.averageRating = val.floatValue();
        getEntityManager().merge(this);
    }

    @Transient
    @JsonProperty("categoryId")
    public long getCategoryId() {
        return this.category.id;
    }
}

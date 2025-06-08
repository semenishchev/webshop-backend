package cc.olek.webshop.shop.search;

import io.quarkus.hibernate.search.orm.elasticsearch.SearchExtension;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

@SearchExtension
public class AnalysisConfigurer implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer("title")
            .custom()
            .tokenizer("standard")
            .tokenFilters(
                "lowercase",
                "asciifolding",
                "remove_duplicates",
                "ngram"
            );

        context.analyzer("keyword")
            .custom()
            .tokenizer("standard")
            .tokenFilters(
                "lowercase",
                "asciifolding",
                "ngram"
            );

        context.analyzer("description")
            .custom()
            .tokenizer("standard")
            .tokenFilters(
                "lowercase",
                "asciifolding"
            );

        context.analyzer("autocomplete")
            .custom()
            .tokenizer("standard")
            .tokenFilters(
                "lowercase",
                "asciifolding",
                "remove_duplicates",
                "porter_stem",
                "edge_ngram"
            );

        context.normalizer("sort")
            .custom()
            .tokenFilters("lowercase", "asciifolding");
    }
}

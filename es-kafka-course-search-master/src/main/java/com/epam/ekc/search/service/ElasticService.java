package com.epam.ekc.search.service;

import com.epam.ekc.search.dto.BookDocument;
import com.epam.ekc.search.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticService {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private final BookDocumentConverter converter;

    public static final String INDEX_NAME = "book_index";
    public static final String INDEX_MAPPING = "{\n" +
            "  \"properties\": {\n" +
            "    \"id\": {\n" +
            "      \"type\": \"text\"\n" +
            "    },\n" +
            "    \"customerId\": {\n" +
            "      \"type\": \"text\"\n" +
            "    },\n" +
            "    \"type\": {\n" +
            "      \"type\": \"keyword\"\n" +
            "    },\n" +
            "    \"genre\": {\n" +
            "      \"type\": \"keyword\"\n" +
            "    },\n" +
            "    \"title\": {\n" +
            "      \"type\": \"text\"\n" +
            "    },\n" +
            "    \"language\": {\n" +
            "      \"type\": \"keyword\"\n" +
            "    },\n" +
            "    \"publicationDate\": {\n" +
            "      \"type\": \"date\",\n" +
            "      \"format\": \"date_optional_time\"\n" +
            "    },\n" +
            "    \"authors\": {\n" +
            "      \"type\": \"text\"\n" +
            "    },\n" +
            "    \"suggest\": {\n" +
            "      \"type\": \"completion\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @PostConstruct
    public void createIndex() throws IOException {
        deleteIndex(INDEX_NAME);
        if (!isIndexExist(INDEX_NAME)) {
            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
            request.mapping(INDEX_MAPPING, XContentType.JSON);
            client.indices().create(request, RequestOptions.DEFAULT);
        }
    }

    private boolean isIndexExist(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    private void deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    public void saveBookToIndex(Book book) throws IOException {
        IndexRequest request = prepareIndexRequest(book, INDEX_NAME);
        client.index(request, RequestOptions.DEFAULT);
    }

    public void saveListOfBooksToIndex(List<Book> books, String indexName) throws IOException {
        BulkRequest request = new BulkRequest();
        for (Book book : books) {
            request.add(prepareIndexRequest(book, indexName));
        }
        client.bulk(request, RequestOptions.DEFAULT);
        System.out.println("Saved " + books.size() + " books");
    }

    private IndexRequest prepareIndexRequest(Book book, String indexName) {
        BookDocument bookDocument = converter.convertToDocument(book);
        Map<String, Object> bookMap = objectMapper.convertValue(bookDocument, Map.class);
        return new IndexRequest(indexName)
                .id(bookDocument.getId())
                .source(bookMap);
    }

    public List<BookDocument> findAll() throws IOException {
        SearchRequest request = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        request.source(searchSourceBuilder);
        request.indices(INDEX_NAME);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        return retrieveResultsFromSearchResponse(client.search(request, RequestOptions.DEFAULT));
    }

    public Map<String, Object> searchByField(String fieldName, String value, int fromResult, int numberOfResults) throws IOException {
        Map<String, Object> result = new HashMap<>();
        SearchResponse response = searchByField(fieldName, value, INDEX_NAME, fromResult, numberOfResults);
        result.put("Total number of found documents", getTotalNumberOfHits(response));
        result.put("Found documents on positions " + fromResult + "-" + (fromResult + numberOfResults), retrieveResultsFromSearchResponse(response));
        return result;
    }


    public SearchResponse searchByField(String fieldName, String value, String indexName, int from, int number) throws IOException {

        SearchRequest request = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(fieldName, value));
        request.source(searchSourceBuilder);
        request.indices(indexName);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(number);

        return client.search(request, RequestOptions.DEFAULT);
    }

    private List<BookDocument> retrieveResultsFromSearchResponse(SearchResponse response) {
        List<BookDocument> books = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            books.add(objectMapper.convertValue(hit.getSourceAsMap(), BookDocument.class));
        }
        return books;
    }

    public long getTotalNumberOfHits(SearchResponse response) {
        TotalHits totalHits = response.getHits().getTotalHits();
        return totalHits.value;
    }

    public Map<String, Long> countByAggregation(String fieldName) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_" + fieldName)
                .field(fieldName);
        searchSourceBuilder.aggregation(aggregation);
        request.source(searchSourceBuilder);

        return retrieveAggregationFromSearchResponse(client.search(request, RequestOptions.DEFAULT), fieldName);
    }

    private Map<String, Long> retrieveAggregationFromSearchResponse(SearchResponse response, String fieldName) {

        Map<String, Long> result = new HashMap<>();
        Aggregations aggregations = response.getAggregations();
        Terms fieldAggregation = aggregations.get("by_" + fieldName);
        for (Terms.Bucket bucket : fieldAggregation.getBuckets()) {
            result.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return result;
    }

    public List<String> autocompleteTitle(String start) throws IOException {
        List<String> suggestions = new ArrayList<>();

        SearchRequest request = new SearchRequest();
        request.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        SuggestionBuilder termSuggestionBuilder =
                SuggestBuilders.completionSuggestion("suggest").prefix(start);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("suggest_title", termSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        request.source(searchSourceBuilder);
        return retrieveSuggestionsFromSearchResponse(client.search(request, RequestOptions.DEFAULT));

    }

    private List<String> retrieveSuggestionsFromSearchResponse(SearchResponse response) {
        List<String> suggestion = new ArrayList<>();
        Suggest suggest = response.getSuggest();
        CompletionSuggestion termSuggestion = suggest.getSuggestion("suggest_title");
        for (CompletionSuggestion.Entry entry : termSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry) {
                suggestion.add(option.getText().string());
            }
        }
        return suggestion;
    }
}


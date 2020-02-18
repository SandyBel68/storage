package com.epam.ekc.search.service;

import com.epam.ekc.search.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StorageService {

    @Value("${booksUrl}")
    private String booksUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Book getBookById(String id){
       return restTemplate.getForObject(booksUrl + id, Book.class);
    }
}

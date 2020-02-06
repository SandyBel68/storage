package com.epam.ekc.search.service;

import com.epam.ekc.search.model.Book;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StorageService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String mainUrl = "http://localhost:10001/api/";

    public Book getBookById(String id){
       return restTemplate.getForObject(mainUrl + "books/" + id, Book.class);
    }
}

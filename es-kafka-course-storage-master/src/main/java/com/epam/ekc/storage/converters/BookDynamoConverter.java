package com.epam.ekc.storage.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.epam.ekc.storage.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class BookDynamoConverter implements DynamoDBTypeConverter<String, Book> {

    @SneakyThrows
    @Override
    public String convert(Book object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    @SneakyThrows
    @Override
    public Book unconvert(String object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, Book.class);
    }
}

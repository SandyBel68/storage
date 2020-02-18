package com.epam.ekc.storage.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.epam.ekc.storage.model.Author;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorDynamoConverter implements DynamoDBTypeConverter<String, List<Author>> {

    @SneakyThrows
    @Override
    public String convert(List<Author> object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    @SneakyThrows
    @Override
    public List<Author> unconvert(String object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, new TypeReference<List<Author>>() {
        });
    }
}

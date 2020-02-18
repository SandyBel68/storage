package com.epam.ekc.storage.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.epam.ekc.storage.model.Author;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorDynamoService {

    private final DynamoDB dynamoDB;
    private final DynamoDBMapper dynamoDBMapper;

    @PostConstruct
    public void init() {
        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Author.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
            dynamoDB.createTable(tableRequest);

        } catch (ResourceInUseException e) {
            log.info("The table Authors already exists");
        } catch (Exception e) {
            log.error("Unable to create table: " + e.getMessage());
        }
    }

    public Author save(Author author) {
        dynamoDBMapper.save(author);
        return author;
    }

    public Author findById(String id) {
        return dynamoDBMapper.load(Author.class, id);
    }

    public List<Author> saveAll(List<Author> authors) {
        for (Author author : authors) {
            save(author);
        }
        return authors;
    }

    public void delete(Author author) {
        dynamoDBMapper.delete(author);
    }

    public void deleteById(String id) {
        delete(findById(id));
    }
}

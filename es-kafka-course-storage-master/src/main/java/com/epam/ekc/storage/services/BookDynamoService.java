package com.epam.ekc.storage.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.epam.ekc.storage.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookDynamoService {

    private final DynamoDB dynamoDB;
    private final DynamoDBMapper dynamoDBMapper;

    @PostConstruct
    public void init() {
        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Book.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
            dynamoDB.createTable(tableRequest);
        } catch (
                ResourceInUseException e) {
            log.info("The table Books already exists");
        } catch (Exception e) {
            log.error("Unable to create table: " + e.getMessage());
        }
    }

    public Book save(Book book) {
        dynamoDBMapper.save(book);
        return book;
    }

    public Book findById(String id) {
        return dynamoDBMapper.load(Book.class, id);
    }

    public List<Book> saveAll(List<Book> books) {
        dynamoDBMapper.batchSave(books);
        return books;
    }

    public void delete(Book book) {
        dynamoDBMapper.delete(book);
    }

    public void deleteById(String id) {
        delete(findById(id));
    }
}

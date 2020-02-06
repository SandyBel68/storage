package com.epam.ekc.search.service;

import com.epam.ekc.search.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final StorageService storageService;
    private final ElasticService elasticService;

    @KafkaListener(id = "single-listener",
            containerFactory = "singleConsumerFactory",
            topics = "storage.entity",
            groupId = "search.batch")
    @SneakyThrows
    public void receiveMessage(List<ConsumerRecord<String, String>> messages) {
        List<Book> books = new ArrayList<>();
        for (ConsumerRecord<String, String> message : messages) {
            var key = message.key();
            Book bookFromMessage = storageService.getBookById(key);
            books.add(bookFromMessage);
        }
        elasticService.saveListOfBooksToIndex(books, ElasticService.INDEX_NAME);
    }
}

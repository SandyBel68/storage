package com.epam.ekc.storage.controller;

import com.epam.ekc.storage.model.Author;
import com.epam.ekc.storage.model.Book;
import com.epam.ekc.storage.model.Identifiable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @SneakyThrows
    public void sendMessage(Identifiable identifiable) {
        var key = identifiable.getId();
        var partition = Math.abs(key.hashCode() % 4);
        var value = identifiable.getClass().getSimpleName();
        if (value.equals("Book")) {
            kafkaTemplate.send("storage.entity", partition, key, value);
        } else {
            log.info("Not supported type");
        }
    }
}

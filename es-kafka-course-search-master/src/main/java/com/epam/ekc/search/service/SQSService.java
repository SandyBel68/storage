package com.epam.ekc.search.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.epam.ekc.search.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSService {

    private final AmazonSQS amazonSQS;
    private final StorageService storageService;
    private final ElasticService elasticService;

    private static final String NAME_QUEUE = "BookQueue";
    private static String queueUrl;

    @PostConstruct
    public void init() {
        queueUrl = amazonSQS.getQueueUrl(NAME_QUEUE).getQueueUrl();
    }

    @SneakyThrows
    @Scheduled(fixedRate = 1000)
    public Book receiveMessage() {
        try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl)
                    .withMaxNumberOfMessages(1)
                    .withWaitTimeSeconds(20);
            List<Message> messages = amazonSQS.receiveMessage(request).getMessages();
            String bookId = messages.get(0).getBody();
            Book bookFromMessage = storageService.getBookById(bookId);
            elasticService.saveBookToIndex(bookFromMessage);
            amazonSQS.deleteMessage(queueUrl, messages.get(0).getReceiptHandle());
            return bookFromMessage;
        } catch (IndexOutOfBoundsException e) {
            log.info("The queue is empty");
            return null;
        }
    }
}

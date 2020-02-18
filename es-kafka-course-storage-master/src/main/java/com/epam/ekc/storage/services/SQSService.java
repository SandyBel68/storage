package com.epam.ekc.storage.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.epam.ekc.storage.model.Identifiable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SQSService {

    private final AmazonSQS amazonSQS;

    private static String queueURL;
    private static final String NAME_QUEUE = "BookQueue";

    @PostConstruct
    public void init(){
        CreateQueueRequest queueRequest = new CreateQueueRequest(NAME_QUEUE);
        queueURL = amazonSQS.createQueue(queueRequest).getQueueUrl();
    }

    public void sendMessage(Identifiable identifiable) {
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(queueURL)
                .withMessageBody(identifiable.getId());

        amazonSQS.sendMessage(sendMessageStandardQueue);
    }
}
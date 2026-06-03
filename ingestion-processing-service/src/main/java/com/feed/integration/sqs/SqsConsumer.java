package com.feed.integration.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feed.model.QueueMessage;
import com.feed.processor.consumer.QueueConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final QueueConsumer queueConsumer;

    @Value("${sqs.queue-url}")
    private String queueUrl;


    @EventListener(ApplicationReadyEvent.class)
    public void startPolling() {

        new Thread(() -> {
            try {
                log.info("Waiting before starting SQS polling...");
                Thread.sleep(5000); // allow ingestion to run first
            } catch (InterruptedException ignored) {}

            log.info("Starting SQS polling...");
            poll();
        }).start();
    }

    private void poll() {

        while (true) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(20)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                for (Message msg : messages) {
                    QueueMessage queueMessage =
                            objectMapper.readValue(msg.body(), QueueMessage.class);

                    try {
                        queueConsumer.consume(queueMessage);

                        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(msg.receiptHandle())
                                .build());

                        log.info("Message processed and deleted. fileId={}", queueMessage.getFileId());

                    } catch (Exception e) {
                        log.error("Processing failed. Will retry via visibility timeout.");
                    }
                }

            } catch (Exception e) {
                log.error("Error polling SQS", e);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
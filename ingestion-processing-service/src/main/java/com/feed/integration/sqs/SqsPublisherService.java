package com.feed.integration.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feed.model.QueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsPublisherService {

    private final SqsClient sqsClient;

    @Value("${sqs.queue-url}")
    private String queueUrl;

    public void publish(QueueMessage message) {

        try {

            String messageBody = toJson(message);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(request);

            System.out.println("Message published to SQS");
            log.info(">>> Publishing to SQS started");
            log.info("Queue URL: {}", queueUrl);
            log.info("Message: {}", message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    private String toJson(Object obj) {
        return """
        {
          "fileId": "%s",
          "advertiserId": "%s",
          "s3Bucket": "%s",
          "s3Key": "%s"
        }
        """.formatted(
                ((QueueMessage) obj).getFileId(),
                ((QueueMessage) obj).getAdvertiserId(),
                ((QueueMessage) obj).getS3Bucket(),
                ((QueueMessage) obj).getS3Key()
        );
    }
}
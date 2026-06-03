package com.feed.processor.service;

import com.feed.model.*;
import com.feed.integration.s3.S3Service;
import com.feed.util.FileParser;
import com.feed.processor.validation.ProductValidator;
import com.feed.processor.retry.RetryHandler;
import com.feed.integration.dynamodb.DynamoBatchWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final S3Service s3Service;
    private final FileParser fileParser;
    private final ProductValidator validator;
    private final RetryHandler retryHandler;
    private final DynamoBatchWriter dynamoBatchWriter;

    public void process(QueueMessage message) {

        log.info("Processing fileId={}", message.getFileId());

        // 1. Download from S3

        // 2. Parse CSV

        InputStream inputStream =
                s3Service.downloadFile(
                        message.getS3Bucket(),
                        message.getS3Key()
                );
        List<Product> products =
                fileParser.parse(inputStream, message.getAdvertiserId());
        // 3. Validate ALL records
        List<Product> validProducts = new ArrayList<>();

        for (Product p : products) {

            p.setFileId(message.getFileId());

            ValidationResult result = validator.validate(p);

            if (result.isValid()) {
                validProducts.add(p);
            } else {
                log.warn("Invalid record skipped: id={}, errors={}",
                        p.getId(), result.getErrors());
            }
        }
        // 4. Retry + Write to DynamoDB
        retryHandler.execute(() ->
        {
            dynamoBatchWriter.write(
                    validProducts,
                    message.getFileId(),
                    message.getAdvertiserId()
            );
            return null;
        });

        log.info("Completed fileId={}", message.getFileId());
    }
}
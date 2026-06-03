package com.feed;

import com.feed.ingestion.FeedScannerService;
import com.feed.integration.s3.S3UploadService;
import com.feed.integration.sqs.SqsPublisherService;
import com.feed.model.QueueMessage;
import com.feed.util.FileIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

    private final FeedScannerService feedScannerService;

    @Override
    public void run(String... args) {
        feedScannerService.scanFeeds();
    }
}
package com.feed.ingestion;

import com.feed.integration.s3.S3UploadService;
import com.feed.integration.sqs.SqsPublisherService;
import com.feed.model.QueueMessage;
import com.feed.util.FileIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class FeedScannerService {

    private final S3UploadService s3UploadService;
    private final SqsPublisherService sqsPublisherService;

    @Value("${feed.root-folder}")
    private String rootFolder;

    // Prevent duplicate processing
    private final Set<String> processedFiles = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedDelayString = "${scanner.fixed-delay-ms}")
    public void scanFeeds() {

        log.info("Starting folder scan. rootFolder={}", rootFolder);

        File rootDir = new File(rootFolder);

        File[] advertiserDirs = rootDir.listFiles(File::isDirectory);

        if (advertiserDirs == null) {
            log.warn("No advertiser folders found");
            return;
        }

        for (File advertiserDir : advertiserDirs) {

            String advertiserId = advertiserDir.getName();

            log.info("Scanning advertiser folder: {}", advertiserId);

            File[] files = advertiserDir.listFiles(file ->
                    file.isFile() && file.getName().endsWith(".csv"));

            if (files == null || files.length == 0) {
                log.info("No CSV files found for advertiser={}", advertiserId);
                continue;
            }

            for (File file : files) {

                //  Skip already processed files
                if (processedFiles.contains(file.getAbsolutePath())) {
                    log.warn("Skipping already processed file: {}", file.getName());
                    continue;
                }

                processedFiles.add(file.getAbsolutePath());

                log.info("Found file: advertiser={}, file={}",
                        advertiserId,
                        file.getName());

                processFile(advertiserId, file);
            }
        }

        log.info("Folder scan completed");
    }

    private void processFile(String advertiserId, File file) {

        try {
            log.info("Processing file START: advertiser={}, file={}",
                    advertiserId, file.getName());

            String fileName = file.getName();
            String s3Key = "uploads/" + advertiserId + "/" + fileName;

            log.info("Uploading to S3. key={}", s3Key);

            // S3 upload
            s3UploadService.uploadFile(file.toPath(), s3Key);

            log.info("S3 upload successful. key={}", s3Key);

            // build message
            String fileId = FileIdGenerator.generate(advertiserId);

            QueueMessage message = QueueMessage.builder()
                    .fileId(fileId)
                    .advertiserId(advertiserId)
                    .s3Bucket("retail-feeds-bucket")
                    .s3Key(s3Key)
                    .build();

            log.info("Publishing message to SQS. fileId={}", fileId);

            // publish to SQS
            sqsPublisherService.publish(message);

            log.info("SQS publish successful. fileId={}", fileId);

            // MOVE TO SUCCESS FOLDER
            moveFile(file, advertiserId, "uploaded-to-s3");

        } catch (Exception e) {

            log.error("Processing failed. advertiser={}, file={}",
                    advertiserId, file.getName(), e);

            // MOVE TO FAILURE FOLDER
            moveFile(file, advertiserId, "failed");
        }
    }

    private void moveFile(File file, String advertiserId, String targetFolder) {

        try {
            File targetDir = new File(file.getParent(), targetFolder);

            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            File targetFile = new File(targetDir, file.getName());

            // instead of renameTo
            Files.move(
                    file.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info("File moved successfully: {} → {}",
                    file.getName(),
                    targetFolder);

        } catch (Exception e) {
            log.error("Exception while moving file: {}", file.getName(), e);
        }
    }
}
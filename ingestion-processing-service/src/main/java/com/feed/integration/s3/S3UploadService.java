package com.feed.integration.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    public void uploadFile(Path filePath, String s3Key) {
        System.out.println("Uploaded file to S3 : " + s3Key);
        log.info("Uploading file to S3. bucket={}, key={}", bucketName, s3Key);
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();

        s3Client.putObject(
                putObjectRequest,
                filePath
        );


        log.info("S3 upload successful. key={}", s3Key);
    }
}
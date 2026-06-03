package com.feed.integration.s3;

import java.io.InputStream;

public interface S3Service {
    InputStream downloadFile(String bucket, String key);
}
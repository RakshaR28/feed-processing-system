package com.feed.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileIdGenerator {

    private FileIdGenerator() {
    }

    public static String generate(String advertiserId) {

        return advertiserId + "_"
                + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
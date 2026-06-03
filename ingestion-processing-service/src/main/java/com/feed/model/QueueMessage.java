package com.feed.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage {

    private String fileId;
    private String advertiserId;
    private String s3Bucket;
    private String s3Key;


}

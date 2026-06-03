package com.feed.processor.consumer;

import com.feed.model.QueueMessage;
import com.feed.processor.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueConsumer {

    private final FileProcessingService fileProcessingService;

    public void consume(QueueMessage message) {

        log.info("Received message for fileId={}", message.getFileId());

        fileProcessingService.process(message);

        log.info("Finished processing fileId={}", message.getFileId());
    }
}
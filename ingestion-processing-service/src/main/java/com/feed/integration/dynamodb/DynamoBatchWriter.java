package com.feed.integration.dynamodb;

import com.feed.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoBatchWriter {

    private final DynamoDbClient dynamoDbClient;

    private static final String TABLE = "retail-products";
    private static final int MAX_BATCH = 25;
    private static final int MAX_RETRY = 5;

    public void write(List<Product> records,
                      String fileId,
                      String advertiserId) {

        for (int i = 0; i < records.size(); i += MAX_BATCH) {

            List<Product> batch =
                    records.subList(i, Math.min(i + MAX_BATCH, records.size()));

            writeBatch(batch, advertiserId, fileId);
        }
    }

    private void writeBatch(List<Product> batch,
                            String advertiserId,
                            String fileId) {

        List<WriteRequest> requests = new ArrayList<>();

        for (Product p : batch) {

            Map<String, AttributeValue> item = new HashMap<>();

            item.put("advertiserId", AttributeValue.fromS(advertiserId));
            item.put("id", AttributeValue.fromS(p.getId()));

            item.put("title", safe(p.getTitle()));
            item.put("description", safe(p.getDescription()));
            item.put("link", safe(p.getLink()));
            item.put("image_link", safe(p.getImageLink()));
            item.put("availability", safe(p.getAvailability()));

            if (p.getPriceAmount() != null) {
                item.put("priceAmount",
                        AttributeValue.fromN(String.valueOf(p.getPriceAmount())));
            }
            item.put("currency", safe(p.getCurrency()));

            item.put("condition", safe(p.getCondition()));
            item.put("fileId", safe(fileId));

            putIfNotNull(item, "brand", p.getBrand());
            putIfNotNull(item, "gtin", p.getGtin());
            putIfNotNull(item, "mpn", p.getMpn());
            putIfNotNull(item, "itemGroupId", p.getItemGroupId());
            putIfNotNull(item, "color", p.getColor());
            putIfNotNull(item, "size", p.getSize());
            putIfNotNull(item, "google_product_category", p.getGoogleProductCategory());

            requests.add(
                    WriteRequest.builder()
                            .putRequest(PutRequest.builder().item(item).build())
                            .build()
            );
        }

        Map<String, List<WriteRequest>> requestMap = Map.of(TABLE, requests);

        int retryCount = 0;

        while (!requestMap.isEmpty() && retryCount < MAX_RETRY) {

            BatchWriteItemResponse response =
                    dynamoDbClient.batchWriteItem(
                            BatchWriteItemRequest.builder()
                                    .requestItems(requestMap)
                                    .build()
                    );

            requestMap = response.unprocessedItems();

            if (!requestMap.isEmpty()) {
                retryCount++;

                try {
                    long backoff = (long) Math.pow(2, retryCount) * 100;
                    log.warn("Retrying unprocessed items, attempt {}", retryCount);
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!requestMap.isEmpty()) {
            throw new RuntimeException("Failed to process all DynamoDB items after retries");
        }
    }

    private AttributeValue safe(String value) {
        return value == null ? AttributeValue.fromS("") : AttributeValue.fromS(value);
    }

    private void putIfNotNull(Map<String, AttributeValue> map,
                              String key,
                              String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, AttributeValue.fromS(value));
        }
    }
}
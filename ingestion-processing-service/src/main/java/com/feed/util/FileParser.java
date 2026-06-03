package com.feed.util;

import com.feed.model.Product;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileParser {

    public List<Product> parse(InputStream inputStream, String advertiserId) {

        List<Product> products = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {

            String[] row;
            boolean isHeader = true;

            while ((row = reader.readNext()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                Product p = new Product();

                p.setAdvertiserId(advertiserId);
                p.setId(row[0]);
                p.setTitle(row[1]);
                p.setDescription(row[2]);
                p.setLink(row[3]);
                p.setImageLink(row[4]);
                p.setAvailability(row[5]);

                // SAFE PRICE PARSE
                if (row[6] != null && row[6].contains(" ")) {
                    String[] parts = row[6].split(" ");
                    try {
                        p.setPriceAmount(Double.parseDouble(parts[0]));
                        p.setCurrency(parts[1]);
                    } catch (Exception e) {
                        p.setPriceAmount(null);
                        p.setCurrency(null);
                    }
                }

                p.setCondition(row[7]);
                p.setBrand(row[8]);
                p.setGtin(row[9]);
                p.setMpn(row[10]);
                p.setItemGroupId(row[11]);
                p.setColor(row[12]);
                p.setSize(row[13]);
                p.setGoogleProductCategory(row[14]);

                products.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("CSV parsing failed", e);
        }

        return products;
    }
}
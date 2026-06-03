package com.feed.processor.validation;

import com.feed.model.Product;
import com.feed.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductValidator {

    public ValidationResult validate(Product p) {

        List<String> errors = new ArrayList<>();

        if (isEmpty(p.getId()) || p.getId().length() > 50)
            errors.add("Invalid id");

        if (isEmpty(p.getTitle()) || p.getTitle().length() > 150)
            errors.add("Invalid title");

        if (isEmpty(p.getDescription()) || p.getDescription().length() > 5000)
            errors.add("Invalid description");

        if (!isValidUrl(p.getLink()))
            errors.add("Invalid link");

        if (!isValidUrl(p.getImageLink()))
            errors.add("Invalid image_link");

        if (!List.of("in_stock","out_of_stock","preorder")
                .contains(p.getAvailability()))
            errors.add("Invalid availability");

        if (p.getPriceAmount() == null || p.getPriceAmount() <= 0)
            errors.add("Invalid price amount");

        if (p.getCurrency() == null || p.getCurrency().isBlank())
            errors.add("Currency missing");

        if (!List.of("new","used","refurbished")
                .contains(p.getCondition()))
            errors.add("Invalid condition");

        if ("new".equals(p.getCondition()) && isEmpty(p.getBrand()))
            errors.add("Brand required");

        if (isEmpty(p.getGtin()) && isEmpty(p.getMpn()))
            errors.add("GTIN or MPN required");

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }

    private boolean isValidUrl(String url) {
        return url != null && url.startsWith("http");
    }

    private boolean isValidPrice(String price) {
        return price != null && price.contains(" ");
    }
}
package com.feed.model;

import lombok.Data;

@Data
public class Product {

    private String advertiserId;
    private String id;

    private String title;
    private String description;

    private String link;
    private String imageLink;

    private String availability;

    private Double priceAmount;
    private String currency;

    private String condition;
    private String brand;

    private String gtin;
    private String mpn;

    private String itemGroupId;
    private String color;
    private String size;

    private String googleProductCategory;

    private String fileId;
}
package com.feed.model;

import lombok.Data;

@Data
public class ProductFeedRecord {

    private String id;
    private String title;
    private String description;
    private String link;
    private String imageLink;
    private String availability;
    private String price;
    private String condition;
    private String brand;
    private String gtin;
    private String mpn;
    private String itemGroupId;
    private String color;
    private String size;
    private String googleProductCategory;
}
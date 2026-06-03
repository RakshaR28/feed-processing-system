# feed-processing-system
Prototype of an advertiser feed ingestion and processing system simulating a pipeline with AWS (S3, SQS, DynamoDB), designed for scalable multi-feed and extensible category-based processing.


## Overview
This project simulates an advertiser feed ingestion and processing pipeline.

## Architecture (MVP)
- Single ingestion API for advertiser feeds
- Validation layer for feed correctness
- Processing layer for normalization
- AWS S3 (raw feed storage)
- AWS SQS (async processing)
- DynamoDB (processed product storage)
- Logging-based failure tracking (email placeholder)

## Design Philosophy
- Start with simplified single-feed ingestion
- Evolve toward multi-feed, multi-category production system
- Maintain extensible schema for future scaling

## Tech Stack
- Java / Spring Boot
- AWS S3, SQS, DynamoDB
- GitHub Actions (CI/CD)

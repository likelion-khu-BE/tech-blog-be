package com.study.shared.s3;

public record PresignedUrlResponse(String presignedUrl, String key) {}
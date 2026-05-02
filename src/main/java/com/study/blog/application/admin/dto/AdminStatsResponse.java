package com.study.blog.application.admin.dto;

public record AdminStatsResponse(
    long totalPosts, long publishedPosts, long draftPosts, long totalComments) {

  public static AdminStatsResponse of(
      long totalPosts, long publishedPosts, long draftPosts, long totalComments) {
    return new AdminStatsResponse(totalPosts, publishedPosts, draftPosts, totalComments);
  }
}

package com.study.blog.application.admin.dto;

public record AdminStatsResponse(
    long totalPosts,
    long draftPosts,
    long pendingReviewPosts,
    long publishedPosts,
    long rejectedPosts,
    long totalComments) {

  public static AdminStatsResponse of(
      long totalPosts,
      long draftPosts,
      long pendingReviewPosts,
      long publishedPosts,
      long rejectedPosts,
      long totalComments) {
    return new AdminStatsResponse(
        totalPosts, draftPosts, pendingReviewPosts, publishedPosts, rejectedPosts, totalComments);
  }
}

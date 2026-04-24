package com.study.blog.admin.dto;

public class AdminStatsResponse {

  private final long totalPosts;
  private final long publishedPosts;
  private final long draftPosts;
  private final long totalComments;

  private AdminStatsResponse(
      long totalPosts, long publishedPosts, long draftPosts, long totalComments) {
    this.totalPosts = totalPosts;
    this.publishedPosts = publishedPosts;
    this.draftPosts = draftPosts;
    this.totalComments = totalComments;
  }

  public static AdminStatsResponse of(
      long totalPosts, long publishedPosts, long draftPosts, long totalComments) {
    return new AdminStatsResponse(totalPosts, publishedPosts, draftPosts, totalComments);
  }

  public long getTotalPosts() {
    return totalPosts;
  }

  public long getPublishedPosts() {
    return publishedPosts;
  }

  public long getDraftPosts() {
    return draftPosts;
  }

  public long getTotalComments() {
    return totalComments;
  }
}

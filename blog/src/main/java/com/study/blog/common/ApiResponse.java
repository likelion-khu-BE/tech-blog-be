package com.study.blog.common;

public class ApiResponse<T> {

  private final T data;
  private final String message;

  private ApiResponse(T data, String message) {
    this.data = data;
    this.message = message;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(null, message);
  }

  public T getData() {
    return data;
  }

  public String getMessage() {
    return message;
  }
}

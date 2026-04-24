package com.study.blog.post;

import com.study.common.entity.Post;
import com.study.common.entity.PostStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

  private PostSpecification() {}

  public static Specification<Post> published() {
    return (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED);
  }

  public static Specification<Post> withBoard(String board) {
    return (root, query, cb) ->
        board == null ? cb.conjunction() : cb.equal(root.get("board"), board);
  }

  public static Specification<Post> withCategory(String category) {
    return (root, query, cb) ->
        category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
  }

  public static Specification<Post> withGeneration(String generation) {
    return (root, query, cb) ->
        generation == null ? cb.conjunction() : cb.equal(root.get("generation"), generation);
  }

  public static Specification<Post> withAuthor(UUID authorId) {
    return (root, query, cb) ->
        authorId == null ? cb.conjunction() : cb.equal(root.get("userId"), authorId);
  }

  public static Specification<Post> withKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return cb.conjunction();
      String pattern = "%" + keyword + "%";
      return cb.or(cb.like(root.get("title"), pattern), cb.like(root.get("content"), pattern));
    };
  }
}

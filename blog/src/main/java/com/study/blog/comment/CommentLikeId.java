package com.study.blog.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeId implements Serializable {

  @Column(name = "comment_id")
  private Long commentId;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentLikeId that)) return false;
    return Objects.equals(commentId, that.commentId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commentId, userId);
  }
}

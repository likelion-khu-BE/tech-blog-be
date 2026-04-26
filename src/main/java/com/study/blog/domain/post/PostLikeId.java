package com.study.blog.domain.post;

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
public class PostLikeId implements Serializable {

  @Column(name = "post_id")
  private Long postId;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostLikeId that)) return false;
    return Objects.equals(postId, that.postId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postId, userId);
  }
}

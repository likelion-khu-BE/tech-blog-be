package com.study.common.entity.post;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostTagId implements Serializable {

  @Column(name = "post_id")
  private Long postId;

  @Column(name = "tag_name", length = 50)
  private String tagName;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostTagId that)) return false;
    return Objects.equals(postId, that.postId) && Objects.equals(tagName, that.tagName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postId, tagName);
  }
}

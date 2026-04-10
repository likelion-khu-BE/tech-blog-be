package com.study.blog.post;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

  @EmbeddedId
  private PostTagId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("postId")
  @JoinColumn(name = "post_id")
  private Post post;

  public PostTag(Post post, String tagName) {
    this.post = post;
    this.id = new PostTagId(post.getId(), tagName);
  }

  public String getTagName() {
    return id.getTagName();
  }
}

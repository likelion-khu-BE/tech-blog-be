package com.study.blog.post;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_bookmarks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostBookmark {

  @EmbeddedId
  private PostBookmarkId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("postId")
  @JoinColumn(name = "post_id")
  private Post post;

  public PostBookmark(Post post, UUID userId) {
    this.post = post;
    this.id = new PostBookmarkId(post.getId(), userId);
  }
}

package com.study.common.entity;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "post_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

  @EmbeddedId
  private PostLikeId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("postId")
  @JoinColumn(name = "post_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Post post;

  public PostLike(Post post, UUID userId) {
    this.post = post;
    this.id = new PostLikeId(post.getId(), userId);
  }
}

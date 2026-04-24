package com.study.common.entity.comment;

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
@Table(name = "comment_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

  @EmbeddedId private CommentLikeId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("commentId")
  @JoinColumn(name = "comment_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Comment comment;

  public CommentLike(Comment comment, UUID userId) {
    this.comment = comment;
    this.id = new CommentLikeId(comment.getId(), userId);
  }
}

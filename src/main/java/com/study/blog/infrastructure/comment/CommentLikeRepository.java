package com.study.blog.infrastructure.comment;

import com.study.blog.domain.comment.CommentLike;
import com.study.blog.domain.comment.CommentLikeId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

  Optional<CommentLike> findByIdCommentIdAndIdUserId(Long commentId, UUID userId);

  long countByIdCommentId(Long commentId);

  @Query(
      "SELECT cl.id.commentId, COUNT(cl) FROM CommentLike cl WHERE cl.id.commentId IN :commentIds GROUP BY cl.id.commentId")
  List<Object[]> countGroupedByCommentId(@Param("commentIds") Collection<Long> commentIds);

  @Query(
      "SELECT cl.id.commentId FROM CommentLike cl WHERE cl.id.commentId IN :commentIds AND cl.id.userId = :userId")
  List<Long> findLikedCommentIds(
      @Param("commentIds") Collection<Long> commentIds, @Param("userId") UUID userId);
}

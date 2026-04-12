package com.study.blog.comment;

import com.study.common.entity.CommentLike;
import com.study.common.entity.CommentLikeId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

  Optional<CommentLike> findByIdCommentIdAndIdUserId(Long commentId, UUID userId);

  long countByIdCommentId(Long commentId);
}

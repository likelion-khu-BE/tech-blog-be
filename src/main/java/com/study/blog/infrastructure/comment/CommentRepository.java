package com.study.blog.infrastructure.comment;

import com.study.blog.domain.comment.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

  long countByPostId(Long postId);
}

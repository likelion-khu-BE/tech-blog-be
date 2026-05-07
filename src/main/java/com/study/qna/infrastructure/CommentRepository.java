package com.study.qna.infrastructure;

import com.study.qna.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByAnswerIdOrderByCreatedAtAsc(Long answerId);
}

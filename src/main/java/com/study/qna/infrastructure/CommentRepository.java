package com.study.qna.infrastructure;

import com.study.qna.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("qnaCommentRepository")
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByAnswer_IdOrderByCreatedAtAsc(Long answerId);
}

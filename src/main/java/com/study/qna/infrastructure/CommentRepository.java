package com.study.qna.infrastructure;

import com.study.qna.domain.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("qnaCommentRepository")
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByAnswer_IdOrderByCreatedAtAsc(Long answerId);

  @Query("SELECT c FROM QnaComment c JOIN FETCH c.answer a JOIN FETCH a.question WHERE c.id = :id")
  Optional<Comment> findByIdWithAnswerAndQuestion(@Param("id") Long id);
}

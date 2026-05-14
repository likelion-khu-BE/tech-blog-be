package com.study.qna.infrastructure;

import com.study.qna.domain.Answer;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

  @Query(
      """
      SELECT a FROM QnaAnswer a
      WHERE a.question.id = :questionId
      ORDER BY a.createdAt ASC
      """)
  List<Answer> findByQuestionId(@Param("questionId") Long questionId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE QnaAnswer a SET a.commentCount = a.commentCount + 1 WHERE a.id = :id")
  void incrementCommentCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(
      "UPDATE QnaAnswer a SET a.commentCount = a.commentCount - 1 WHERE a.id = :id AND a.commentCount > 0")
  void decrementCommentCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE QnaAnswer a SET a.voteCount = a.voteCount + :delta WHERE a.id = :answerId")
  void updateVoteCount(@Param("answerId") Long answerId, @Param("delta") int delta);
}
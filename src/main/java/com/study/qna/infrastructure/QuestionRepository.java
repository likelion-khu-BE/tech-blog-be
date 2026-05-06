package com.study.qna.infrastructure;

import com.study.qna.domain.Question;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

  @Query(
      """
      SELECT DISTINCT q
      FROM QnaQuestion q
      LEFT JOIN FETCH q.questionTags qt
      LEFT JOIN FETCH qt.tag
      ORDER BY q.createdAt DESC
      """)
  List<Question> findAllWithTags();

  @Query(
      """
      SELECT q
      FROM QnaQuestion q
      LEFT JOIN FETCH q.questionTags qt
      LEFT JOIN FETCH qt.tag
      WHERE q.id = :id
      """)
  Optional<Question> findByIdWithTags(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE QnaQuestion q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id")
  void incrementViewCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE QnaQuestion q SET q.answerCount = q.answerCount + 1 WHERE q.id = :id")
  void incrementAnswerCount(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(
      "UPDATE QnaQuestion q SET q.answerCount = q.answerCount - 1 WHERE q.id = :id AND q.answerCount > 0")
  void decrementAnswerCount(@Param("id") Long id);
}

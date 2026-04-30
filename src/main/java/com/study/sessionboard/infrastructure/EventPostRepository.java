package com.study.sessionboard.infrastructure;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPostRepository extends JpaRepository<EventPost, Long> {

  // 기수별 전체 조회 (발행일 최신순)
  List<EventPost> findByGenerationIdOrderByPublishedAtDesc(Long generationId);

  // 발행된 것만 조회
  List<EventPost> findByGenerationIdAndStatusOrderByPublishedAtDesc(
      Long generationId, EventPostStatus status);

  // 작성자별 조회
  List<EventPost> findByAuthorIdOrderByPublishedAtDesc(Long authorId);

  // 이미지 포함 단건 조회 (N+1 방지)
  @Query(
      "SELECT DISTINCT p FROM EventPost p "
          + "LEFT JOIN FETCH p.author "
          + "LEFT JOIN FETCH p.generation "
          + "WHERE p.id = :id")
  java.util.Optional<EventPost> findWithDetailsById(@Param("id") Long id);
}

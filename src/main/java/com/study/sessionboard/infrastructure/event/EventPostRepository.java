package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import com.study.sessionboard.domain.event.EventPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPostRepository extends JpaRepository<EventPost, Long> {

  @Query(
      """
      SELECT p FROM EventPost p
      JOIN FETCH p.author
      WHERE p.generation.number = :generationNumber
        AND p.status = :status
        AND (:type IS NULL OR p.type = :type)
      ORDER BY p.createdAt DESC
      """)
  Page<EventPost> findAllWithFilters(
      @Param("generationNumber") Integer generationNumber, // generation number로 시현 수정
      @Param("status") EventPostStatus status,
      @Param("type") EventPostType type,
      Pageable pageable);
}

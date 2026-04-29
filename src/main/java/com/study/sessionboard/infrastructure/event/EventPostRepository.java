package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostType;
import java.time.LocalDate;
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
      WHERE (:type IS NULL OR p.type = :type)
        AND (:date IS NULL OR p.eventDate = :date)
      ORDER BY p.createdAt DESC
      """)
  Page<EventPost> findAllWithFilters(
      @Param("type") EventPostType type,
      @Param("date") LocalDate date,
      Pageable pageable);
}

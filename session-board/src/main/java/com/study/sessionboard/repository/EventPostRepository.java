package com.study.sessionboard.repository;

import com.study.common.entity.EventPost;
import com.study.common.entity.PostStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPostRepository extends JpaRepository<EventPost, UUID> {

  List<EventPost> findByGenerationIdOrderByPublishedAtDesc(Integer generationId);

  List<EventPost> findByAuthorIdOrderByPublishedAtDesc(UUID authorId);

  List<EventPost> findByGenerationIdAndStatusOrderByPublishedAtDesc(
      Integer generationId, PostStatus status);
}

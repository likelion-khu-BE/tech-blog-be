package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPostImageRepository extends JpaRepository<EventPostImage, Long> {

  List<EventPostImage> findByPostIdOrderByOrderAsc(Long postId);

  List<EventPostImage> findByPostIdInOrderByOrderAsc(List<Long> postIds);

  void deleteByPostId(Long postId);
}

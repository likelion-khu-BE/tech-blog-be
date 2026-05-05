package com.study.sessionboard.infrastructure;

import com.study.sessionboard.domain.event.EventPostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPostImageRepository extends JpaRepository<EventPostImage, Long> {
  List<EventPostImage> findAllByPostIdOrderByOrderAsc(Long postId);
}

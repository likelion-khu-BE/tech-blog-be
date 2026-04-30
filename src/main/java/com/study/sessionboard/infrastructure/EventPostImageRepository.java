package com.study.sessionboard.infrastructure;

import com.study.sessionboard.domain.event.EventPostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPostImageRepository extends JpaRepository<EventPostImage, Long> {

  List<EventPostImage> findByPostIdOrderByOrder(Long postId);
}

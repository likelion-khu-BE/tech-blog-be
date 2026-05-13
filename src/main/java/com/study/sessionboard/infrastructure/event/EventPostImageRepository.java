package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPostImageRepository extends JpaRepository<EventPostImage, Long> {

  /** 게시글당 첫 번째 이미지(order 최솟값)만 조회 — 목록 썸네일용 */
  @Query(
      """
      SELECT i FROM EventPostImage i
      WHERE i.post.id IN :postIds
        AND i.order = (
          SELECT MIN(i2.order) FROM EventPostImage i2 WHERE i2.post.id = i.post.id
        )
      """)
  List<EventPostImage> findFirstImagesByPostIdIn(@Param("postIds") List<Long> postIds);

  void deleteByPostId(Long postId);
}
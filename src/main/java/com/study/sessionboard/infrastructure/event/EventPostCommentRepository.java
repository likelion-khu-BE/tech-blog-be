package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPostComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventPostCommentRepository extends JpaRepository<EventPostComment, Long> {

  @Query(
      "SELECT c.post.id, COUNT(c) FROM EventPostComment c "
          + "WHERE c.post.id IN :postIds GROUP BY c.post.id")
  List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);

  List<EventPostComment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

  List<EventPostComment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);
}

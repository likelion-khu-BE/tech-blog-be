package com.study.blog.infrastructure.post;

import com.study.blog.domain.post.PostBookmark;
import com.study.blog.domain.post.PostBookmarkId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostBookmarkId> {

  Optional<PostBookmark> findByIdPostIdAndIdUserId(Long postId, Long userId);

  long countByIdPostId(Long postId);

  @Query("SELECT b.id.postId FROM PostBookmark b WHERE b.id.userId = :userId")
  List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}

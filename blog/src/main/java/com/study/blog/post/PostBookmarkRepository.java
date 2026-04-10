package com.study.blog.post;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostBookmarkId> {

  Optional<PostBookmark> findByIdPostIdAndIdUserId(Long postId, UUID userId);

  long countByIdPostId(Long postId);
}

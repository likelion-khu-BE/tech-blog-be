package com.study.blog.infrastructure.post;

import com.study.blog.domain.post.PostBookmark;
import com.study.blog.domain.post.PostBookmarkId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostBookmarkId> {

  Optional<PostBookmark> findByIdPostIdAndIdUserId(Long postId, UUID userId);

  long countByIdPostId(Long postId);
}

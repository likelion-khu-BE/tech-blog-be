package com.study.blog.post;

import com.study.common.entity.PostBookmark;
import com.study.common.entity.PostBookmarkId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, PostBookmarkId> {

  Optional<PostBookmark> findByIdPostIdAndIdUserId(Long postId, UUID userId);

  long countByIdPostId(Long postId);
}

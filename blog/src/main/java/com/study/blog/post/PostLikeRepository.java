package com.study.blog.post;

import com.study.common.entity.PostLike;
import com.study.common.entity.PostLikeId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

  Optional<PostLike> findByIdPostIdAndIdUserId(Long postId, UUID userId);

  long countByIdPostId(Long postId);

  List<PostLike> findByIdPostIdIn(Collection<Long> postIds);
}

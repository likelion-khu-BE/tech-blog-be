package com.study.blog.post;

import com.study.common.entity.post.PostLike;
import com.study.common.entity.post.PostLikeId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

  Optional<PostLike> findByIdPostIdAndIdUserId(Long postId, UUID userId);

  long countByIdPostId(Long postId);

  @Query(
      "SELECT pl.id.postId, COUNT(pl) FROM PostLike pl WHERE pl.id.postId IN :postIds GROUP BY pl.id.postId")
  List<Object[]> countGroupedByPostId(@Param("postIds") Collection<Long> postIds);
}

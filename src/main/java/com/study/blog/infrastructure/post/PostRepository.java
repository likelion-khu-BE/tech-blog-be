package com.study.blog.infrastructure.post;

import com.study.blog.domain.post.Post;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

  long countByUserId(Long userId);

  Page<Post> findAllByIdIn(Collection<Long> ids, Pageable pageable);
}

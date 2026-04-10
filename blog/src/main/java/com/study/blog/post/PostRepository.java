package com.study.blog.post;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository
    extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

  long countByUserId(UUID userId);
}

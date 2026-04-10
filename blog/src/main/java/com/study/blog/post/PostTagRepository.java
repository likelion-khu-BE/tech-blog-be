package com.study.blog.post;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

  List<PostTag> findByPost(Post post);

  void deleteByPost(Post post);
}

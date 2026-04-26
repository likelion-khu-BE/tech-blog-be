package com.study.blog.infrastructure.post;

import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostTag;
import com.study.blog.domain.post.PostTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

  List<PostTag> findByPost(Post post);

  List<PostTag> findByIdPostIdIn(Collection<Long> postIds);

  void deleteByPost(Post post);
}

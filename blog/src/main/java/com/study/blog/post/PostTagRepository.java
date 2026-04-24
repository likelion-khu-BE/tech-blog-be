package com.study.blog.post;

import com.study.common.entity.Post;
import com.study.common.entity.PostTag;
import com.study.common.entity.PostTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

  List<PostTag> findByPost(Post post);

  List<PostTag> findByIdPostIdIn(Collection<Long> postIds);

  void deleteByPost(Post post);
}

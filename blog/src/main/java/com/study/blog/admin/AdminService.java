package com.study.blog.admin;

import com.study.blog.admin.dto.AdminPostResponse;
import com.study.blog.admin.dto.AdminStatsResponse;
import com.study.blog.comment.CommentRepository;
import com.study.blog.common.exception.BlogErrorCode;
import com.study.blog.common.exception.BlogException;
import com.study.blog.post.Post;
import com.study.blog.post.PostLikeRepository;
import com.study.blog.post.PostRepository;
import com.study.blog.post.PostStatus;
import com.study.blog.post.PostTag;
import com.study.blog.post.PostTagRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

  private final PostRepository postRepository;
  private final PostTagRepository postTagRepository;
  private final PostLikeRepository postLikeRepository;
  private final CommentRepository commentRepository;

  public AdminService(
      PostRepository postRepository,
      PostTagRepository postTagRepository,
      PostLikeRepository postLikeRepository,
      CommentRepository commentRepository) {
    this.postRepository = postRepository;
    this.postTagRepository = postTagRepository;
    this.postLikeRepository = postLikeRepository;
    this.commentRepository = commentRepository;
  }

  public AdminStatsResponse getStats() {
    long totalPosts = postRepository.count();
    long publishedPosts = postRepository.count(
        (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED));
    long draftPosts = totalPosts - publishedPosts;
    long totalComments = commentRepository.count();
    return AdminStatsResponse.of(totalPosts, publishedPosts, draftPosts, totalComments);
  }

  public Page<AdminPostResponse> getAllPosts(int page, int size) {
    return postRepository
        .findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
        .map(this::toAdminPost);
  }

  @Transactional
  public void changePostStatus(Long postId, PostStatus status) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BlogException(BlogErrorCode.POST_NOT_FOUND));
    post.changeStatus(status);
  }

  @Transactional
  public void forceDeletePost(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BlogException(BlogErrorCode.POST_NOT_FOUND));
    postTagRepository.deleteByPost(post);
    postRepository.delete(post);
  }

  private AdminPostResponse toAdminPost(Post post) {
    List<String> tags =
        postTagRepository.findByPost(post).stream().map(PostTag::getTagName).toList();
    long likeCount = postLikeRepository.countByIdPostId(post.getId());
    return AdminPostResponse.of(post, tags, likeCount);
  }
}

package com.study.blog.application.admin;

import com.study.blog.application.admin.dto.AdminPostResponse;
import com.study.blog.application.admin.dto.AdminStatsResponse;
import com.study.blog.application.admin.dto.PostStatusUpdateRequest;
import com.study.blog.domain.post.Post;
import com.study.blog.domain.post.PostStatus;
import com.study.blog.domain.post.PostTag;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.infrastructure.post.PostLikeRepository;
import com.study.blog.infrastructure.post.PostRepository;
import com.study.blog.infrastructure.post.PostTagRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    long draftPosts =
        postRepository.count((root, query, cb) -> cb.equal(root.get("status"), PostStatus.DRAFT));
    long pendingReviewPosts =
        postRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PENDING_REVIEW));
    long publishedPosts =
        postRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED));
    long rejectedPosts =
        postRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), PostStatus.REJECTED));
    long totalComments = commentRepository.count();
    return AdminStatsResponse.of(
        totalPosts, draftPosts, pendingReviewPosts, publishedPosts, rejectedPosts, totalComments);
  }

  public Page<AdminPostResponse> getAllPosts(PostStatus status, int page, int size) {
    Specification<Post> spec =
        status == null
            ? Specification.where(null)
            : (root, query, cb) -> cb.equal(root.get("status"), status);
    Page<Post> posts =
        postRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));

    List<Long> postIds = posts.stream().map(Post::getId).toList();
    Map<Long, List<String>> tagsByPostId = batchTagsByPostId(postIds);
    Map<Long, Long> likeCountByPostId = batchLikeCountByPostId(postIds);

    return posts.map(
        post ->
            AdminPostResponse.of(
                post,
                tagsByPostId.getOrDefault(post.getId(), List.of()),
                likeCountByPostId.getOrDefault(post.getId(), 0L)));
  }

  @Transactional
  public AdminPostResponse changePostStatus(Long postId, PostStatusUpdateRequest req) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BlogException(BlogErrorCode.POST_NOT_FOUND));
    switch (req.status()) {
      case PUBLISHED -> post.publish();
      case REJECTED -> {
        if (req.reason() == null || req.reason().isBlank()) {
          throw new BlogException(BlogErrorCode.REJECTION_REASON_REQUIRED);
        }
        post.reject(req.reason());
      }
      default -> post.changeStatus(req.status());
    }
    List<String> tags =
        postTagRepository.findByPost(post).stream().map(PostTag::getTagName).toList();
    long likeCount = postLikeRepository.countByIdPostId(postId);
    return AdminPostResponse.of(post, tags, likeCount);
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

  private Map<Long, List<String>> batchTagsByPostId(Collection<Long> postIds) {
    return postTagRepository.findByIdPostIdIn(postIds).stream()
        .collect(
            Collectors.groupingBy(
                t -> t.getId().getPostId(),
                Collectors.mapping(PostTag::getTagName, Collectors.toList())));
  }

  private Map<Long, Long> batchLikeCountByPostId(Collection<Long> postIds) {
    return postLikeRepository.countGroupedByPostId(postIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }
}

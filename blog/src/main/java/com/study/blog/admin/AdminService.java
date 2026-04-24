package com.study.blog.admin;

import com.study.blog.admin.dto.AdminPostResponse;
import com.study.blog.admin.dto.AdminStatsResponse;
import com.study.blog.comment.CommentRepository;
import com.study.blog.post.PostLikeRepository;
import com.study.blog.post.PostRepository;
import com.study.blog.post.PostTagRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import com.study.common.entity.Post;
import com.study.common.entity.PostStatus;
import com.study.common.entity.PostTag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    long publishedPosts =
        postRepository.count(
            (root, query, cb) -> cb.equal(root.get("status"), PostStatus.PUBLISHED));
    long draftPosts = totalPosts - publishedPosts;
    long totalComments = commentRepository.count();
    return AdminStatsResponse.of(totalPosts, publishedPosts, draftPosts, totalComments);
  }

  public Page<AdminPostResponse> getAllPosts(int page, int size) {
    Page<Post> posts =
        postRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));

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

package com.study.blog.post;

import com.study.blog.common.exception.BlogErrorCode;
import com.study.blog.common.exception.BlogException;
import com.study.blog.post.dto.PostCreateRequest;
import com.study.blog.post.dto.PostResponse;
import com.study.blog.post.dto.PostSummaryResponse;
import com.study.blog.post.dto.PostUpdateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

  private final PostRepository postRepository;
  private final PostTagRepository postTagRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostBookmarkRepository postBookmarkRepository;

  public PostService(
      PostRepository postRepository,
      PostTagRepository postTagRepository,
      PostLikeRepository postLikeRepository,
      PostBookmarkRepository postBookmarkRepository) {
    this.postRepository = postRepository;
    this.postTagRepository = postTagRepository;
    this.postLikeRepository = postLikeRepository;
    this.postBookmarkRepository = postBookmarkRepository;
  }

  public Page<PostSummaryResponse> getPosts(
      String board,
      String category,
      String generation,
      UUID authorId,
      String keyword,
      int page,
      int size) {
    Specification<Post> spec =
        Specification.where(PostSpecification.published())
            .and(PostSpecification.withBoard(board))
            .and(PostSpecification.withCategory(category))
            .and(PostSpecification.withGeneration(generation))
            .and(PostSpecification.withAuthor(authorId))
            .and(PostSpecification.withKeyword(keyword));

    return postRepository
        .findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()))
        .map(post -> toSummary(post));
  }

  public PostResponse getPost(Long postId, UUID requesterId) {
    Post post = findById(postId);

    if (post.getStatus() == PostStatus.DRAFT && !post.getUserId().equals(requesterId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }

    return toResponse(post, requesterId);
  }

  @Transactional
  public PostResponse createPost(PostCreateRequest req, UUID userId) {
    Post post =
        Post.builder()
            .userId(userId)
            .title(req.getTitle())
            .content(req.getContent())
            .board(req.getBoard())
            .category(req.getCategory())
            .status(req.getStatus())
            .generation(req.getGeneration())
            .repostFromId(req.getRepostFromId())
            .build();
    post = postRepository.save(post);

    saveTags(post, req.getTags());
    return toResponse(post, userId);
  }

  @Transactional
  public PostResponse updatePost(Long postId, PostUpdateRequest req, UUID userId) {
    Post post = findById(postId);
    if (!post.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }

    post.update(req.getTitle(), req.getContent(), req.getBoard(), req.getCategory(), req.getStatus());

    postTagRepository.deleteByPost(post);
    saveTags(post, req.getTags());

    return toResponse(post, userId);
  }

  @Transactional
  public void deletePost(Long postId, UUID userId) {
    Post post = findById(postId);
    if (!post.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }
    postTagRepository.deleteByPost(post);
    postRepository.delete(post);
  }

  @Transactional
  public boolean toggleLike(Long postId, UUID userId) {
    Post post = findById(postId);
    return postLikeRepository
        .findByIdPostIdAndIdUserId(postId, userId)
        .map(
            like -> {
              postLikeRepository.delete(like);
              return false;
            })
        .orElseGet(
            () -> {
              postLikeRepository.save(new PostLike(post, userId));
              return true;
            });
  }

  @Transactional
  public boolean toggleBookmark(Long postId, UUID userId) {
    Post post = findById(postId);
    return postBookmarkRepository
        .findByIdPostIdAndIdUserId(postId, userId)
        .map(
            bookmark -> {
              postBookmarkRepository.delete(bookmark);
              return false;
            })
        .orElseGet(
            () -> {
              postBookmarkRepository.save(new PostBookmark(post, userId));
              return true;
            });
  }

  private Post findById(Long postId) {
    return postRepository
        .findById(postId)
        .orElseThrow(() -> new BlogException(BlogErrorCode.POST_NOT_FOUND));
  }

  private void saveTags(Post post, List<String> tags) {
    if (tags == null || tags.isEmpty()) return;
    tags.stream()
        .distinct()
        .map(tag -> new PostTag(post, tag))
        .forEach(postTagRepository::save);
  }

  private List<String> getTagNames(Post post) {
    return postTagRepository.findByPost(post).stream().map(PostTag::getTagName).toList();
  }

  private PostSummaryResponse toSummary(Post post) {
    long likeCount = postLikeRepository.countByIdPostId(post.getId());
    return PostSummaryResponse.of(post, getTagNames(post), likeCount);
  }

  private PostResponse toResponse(Post post, UUID requesterId) {
    List<String> tags = getTagNames(post);
    long likeCount = postLikeRepository.countByIdPostId(post.getId());
    long bookmarkCount = postBookmarkRepository.countByIdPostId(post.getId());
    boolean liked =
        postLikeRepository.findByIdPostIdAndIdUserId(post.getId(), requesterId).isPresent();
    boolean bookmarked =
        postBookmarkRepository
            .findByIdPostIdAndIdUserId(post.getId(), requesterId)
            .isPresent();
    return PostResponse.of(post, tags, likeCount, bookmarkCount, liked, bookmarked);
  }
}

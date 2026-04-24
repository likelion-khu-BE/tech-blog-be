package com.study.blog.post;

import com.study.blog.post.dto.PostCreateRequest;
import com.study.blog.post.dto.PostResponse;
import com.study.blog.post.dto.PostSummaryResponse;
import com.study.blog.post.dto.PostUpdateRequest;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import com.study.common.entity.post.Post;
import com.study.common.entity.post.PostBookmark;
import com.study.common.entity.post.PostLike;
import com.study.common.entity.post.PostStatus;
import com.study.common.entity.post.PostTag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
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

    Page<Post> posts =
        postRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()));

    List<Long> postIds = posts.stream().map(Post::getId).toList();
    Map<Long, List<String>> tagsByPostId = batchTagsByPostId(postIds);
    Map<Long, Long> likeCountByPostId = batchLikeCountByPostId(postIds);

    return posts.map(
        post ->
            PostSummaryResponse.of(
                post,
                tagsByPostId.getOrDefault(post.getId(), List.of()),
                likeCountByPostId.getOrDefault(post.getId(), 0L)));
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

    post.update(
        req.getTitle(), req.getContent(), req.getBoard(), req.getCategory(), req.getStatus());

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
              try {
                postLikeRepository.saveAndFlush(new PostLike(post, userId));
              } catch (DataIntegrityViolationException ignored) {
                // concurrent insert — already liked
              }
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
              try {
                postBookmarkRepository.saveAndFlush(new PostBookmark(post, userId));
              } catch (DataIntegrityViolationException ignored) {
                // concurrent insert — already bookmarked
              }
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
    tags.stream().distinct().map(tag -> new PostTag(post, tag)).forEach(postTagRepository::save);
  }

  private PostResponse toResponse(Post post, UUID requesterId) {
    List<String> tags =
        postTagRepository.findByPost(post).stream().map(PostTag::getTagName).toList();
    long likeCount = postLikeRepository.countByIdPostId(post.getId());
    long bookmarkCount = postBookmarkRepository.countByIdPostId(post.getId());
    boolean liked =
        requesterId != null
            && postLikeRepository.findByIdPostIdAndIdUserId(post.getId(), requesterId).isPresent();
    boolean bookmarked =
        requesterId != null
            && postBookmarkRepository
                .findByIdPostIdAndIdUserId(post.getId(), requesterId)
                .isPresent();
    return PostResponse.of(post, tags, likeCount, bookmarkCount, liked, bookmarked);
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

package com.study.blog.application.comment;

import com.study.blog.application.comment.dto.CommentCreateRequest;
import com.study.blog.application.comment.dto.CommentResponse;
import com.study.blog.application.comment.dto.CommentUpdateRequest;
import com.study.blog.domain.comment.Comment;
import com.study.blog.domain.comment.CommentLike;
import com.study.blog.infrastructure.comment.CommentLikeRepository;
import com.study.blog.infrastructure.comment.CommentRepository;
import com.study.blog.shared.exception.BlogErrorCode;
import com.study.blog.shared.exception.BlogException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;

  public CommentService(
      CommentRepository commentRepository, CommentLikeRepository commentLikeRepository) {
    this.commentRepository = commentRepository;
    this.commentLikeRepository = commentLikeRepository;
  }

  public List<CommentResponse> getComments(Long postId, Long requesterId) {
    List<Comment> all = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);
    if (all.isEmpty()) {
      return List.of();
    }

    List<Long> commentIds = all.stream().map(Comment::getId).toList();

    Map<Long, Long> likeCountByCommentId =
        commentLikeRepository.countGroupedByCommentId(commentIds).stream()
            .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    Map<Long, Boolean> likedByCommentId =
        requesterId == null
            ? Map.of()
            : commentLikeRepository.findLikedCommentIds(commentIds, requesterId).stream()
                .collect(Collectors.toMap(id -> id, id -> true));

    Map<Long, List<Comment>> childrenByParentId =
        all.stream()
            .filter(c -> c.getParentId() != null)
            .collect(Collectors.groupingBy(Comment::getParentId));

    return all.stream()
        .filter(c -> c.getParentId() == null)
        .sorted(Comparator.comparing(Comment::getCreatedAt))
        .map(root -> buildTree(root, childrenByParentId, likeCountByCommentId, likedByCommentId))
        .toList();
  }

  @Transactional
  public CommentResponse createComment(Long postId, CommentCreateRequest req, Long userId) {
    Comment parent = null;
    if (req.parentId() != null) {
      parent =
          commentRepository
              .findById(req.parentId())
              .orElseThrow(() -> new BlogException(BlogErrorCode.PARENT_COMMENT_NOT_FOUND));
      if (!parent.getPostId().equals(postId)) {
        throw new BlogException(BlogErrorCode.PARENT_COMMENT_NOT_FOUND);
      }
    }

    Comment comment =
        Comment.builder()
            .postId(postId)
            .userId(userId)
            .parent(parent)
            .content(req.content())
            .build();
    comment = commentRepository.save(comment);
    return CommentResponse.of(comment, 0, false, List.of());
  }

  @Transactional
  public CommentResponse updateComment(Long commentId, CommentUpdateRequest req, Long userId) {
    Comment comment = findById(commentId);
    if (!comment.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }
    comment.updateContent(req.content());
    long likeCount = commentLikeRepository.countByIdCommentId(commentId);
    boolean liked =
        commentLikeRepository.findByIdCommentIdAndIdUserId(commentId, userId).isPresent();
    return CommentResponse.of(comment, likeCount, liked, List.of());
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = findById(commentId);
    if (!comment.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }
    comment.softDelete();
  }

  @Transactional
  public boolean toggleLike(Long commentId, Long userId) {
    Comment comment = findById(commentId);
    return commentLikeRepository
        .findByIdCommentIdAndIdUserId(commentId, userId)
        .map(
            like -> {
              commentLikeRepository.delete(like);
              return false;
            })
        .orElseGet(
            () -> {
              try {
                commentLikeRepository.saveAndFlush(new CommentLike(comment, userId));
              } catch (DataIntegrityViolationException ignored) {
                // concurrent insert — already liked
              }
              return true;
            });
  }

  private CommentResponse buildTree(
      Comment comment,
      Map<Long, List<Comment>> childrenByParentId,
      Map<Long, Long> likeCountByCommentId,
      Map<Long, Boolean> likedByCommentId) {
    List<CommentResponse> replies =
        childrenByParentId.getOrDefault(comment.getId(), List.of()).stream()
            .sorted(Comparator.comparing(Comment::getCreatedAt))
            .map(
                child ->
                    buildTree(child, childrenByParentId, likeCountByCommentId, likedByCommentId))
            .toList();
    return CommentResponse.of(
        comment,
        likeCountByCommentId.getOrDefault(comment.getId(), 0L),
        likedByCommentId.getOrDefault(comment.getId(), false),
        replies);
  }

  private Comment findById(Long commentId) {
    return commentRepository
        .findById(commentId)
        .filter(c -> !c.isDeleted())
        .orElseThrow(() -> new BlogException(BlogErrorCode.COMMENT_NOT_FOUND));
  }
}

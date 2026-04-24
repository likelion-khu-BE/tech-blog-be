package com.study.blog.comment;

import com.study.blog.comment.dto.CommentCreateRequest;
import com.study.blog.comment.dto.CommentResponse;
import com.study.blog.comment.dto.CommentUpdateRequest;
import com.study.blog.common.exception.BlogErrorCode;
import com.study.blog.common.exception.BlogException;
import com.study.common.entity.Comment;
import com.study.common.entity.CommentLike;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  public List<CommentResponse> getComments(Long postId, UUID requesterId) {
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

    Map<Long, CommentResponse> map = new LinkedHashMap<>();
    List<CommentResponse> roots = new ArrayList<>();

    for (Comment c : all) {
      long likeCount = likeCountByCommentId.getOrDefault(c.getId(), 0L);
      boolean liked = likedByCommentId.getOrDefault(c.getId(), false);
      CommentResponse resp = CommentResponse.of(c, likeCount, liked);
      map.put(c.getId(), resp);
      if (c.getParentId() == null) {
        roots.add(resp);
      }
    }

    for (Comment c : all) {
      if (c.getParentId() != null) {
        CommentResponse parent = map.get(c.getParentId());
        if (parent != null) {
          parent.addReply(map.get(c.getId()));
        }
      }
    }

    return roots;
  }

  @Transactional
  public CommentResponse createComment(Long postId, CommentCreateRequest req, UUID userId) {
    Comment parent = null;
    if (req.getParentId() != null) {
      parent =
          commentRepository
              .findById(req.getParentId())
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
            .content(req.getContent())
            .build();
    comment = commentRepository.save(comment);
    return CommentResponse.of(comment, 0, false);
  }

  @Transactional
  public CommentResponse updateComment(Long commentId, CommentUpdateRequest req, UUID userId) {
    Comment comment = findById(commentId);
    if (!comment.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }
    comment.updateContent(req.getContent());
    long likeCount = commentLikeRepository.countByIdCommentId(commentId);
    boolean liked =
        commentLikeRepository.findByIdCommentIdAndIdUserId(commentId, userId).isPresent();
    return CommentResponse.of(comment, likeCount, liked);
  }

  @Transactional
  public void deleteComment(Long commentId, UUID userId) {
    Comment comment = findById(commentId);
    if (!comment.getUserId().equals(userId)) {
      throw new BlogException(BlogErrorCode.FORBIDDEN);
    }
    commentRepository.delete(comment);
  }

  @Transactional
  public boolean toggleLike(Long commentId, UUID userId) {
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

  private Comment findById(Long commentId) {
    return commentRepository
        .findById(commentId)
        .orElseThrow(() -> new BlogException(BlogErrorCode.COMMENT_NOT_FOUND));
  }
}

package com.study.blog.comment;

import com.study.blog.comment.dto.CommentCreateRequest;
import com.study.blog.comment.dto.CommentResponse;
import com.study.blog.comment.dto.CommentUpdateRequest;
import com.study.blog.common.exception.BlogErrorCode;
import com.study.blog.common.exception.BlogException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    Map<Long, CommentResponse> map = new LinkedHashMap<>();
    List<CommentResponse> roots = new ArrayList<>();

    for (Comment c : all) {
      long likeCount = commentLikeRepository.countByIdCommentId(c.getId());
      boolean liked =
          requesterId != null
              && commentLikeRepository
                  .findByIdCommentIdAndIdUserId(c.getId(), requesterId)
                  .isPresent();
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
  public CommentResponse createComment(
      Long postId, CommentCreateRequest req, UUID userId) {
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
    return CommentResponse.of(comment, likeCount, false);
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
              commentLikeRepository.save(new CommentLike(comment, userId));
              return true;
            });
  }

  private Comment findById(Long commentId) {
    return commentRepository
        .findById(commentId)
        .orElseThrow(() -> new BlogException(BlogErrorCode.COMMENT_NOT_FOUND));
  }
}

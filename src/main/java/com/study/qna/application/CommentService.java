package com.study.qna.application;

import com.study.qna.application.dto.request.comment.CommentCreateRequest;
import com.study.qna.application.dto.request.comment.CommentUpdateRequest;
import com.study.qna.application.dto.response.comment.CommentResponse;
import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Comment;
import com.study.qna.domain.exception.AnswerNotFoundException;
import com.study.qna.domain.exception.CommentNotFoundException;
import com.study.qna.domain.exception.ForbiddenQnaActionException;
import com.study.qna.infrastructure.AnswerRepository;
import com.study.qna.infrastructure.CommentRepository;
import com.study.shared.extevent.qna.QnaCommentCreated;
import com.study.shared.extevent.qna.QnaCommentDeleted;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service("qnaCommentService")
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final AnswerRepository answerRepository;
  private final ApplicationEventPublisher eventPublisher;

  public List<CommentResponse> getComments(Long answerId) {
    answerRepository.findById(answerId).orElseThrow(() -> new AnswerNotFoundException(answerId));
    return commentRepository.findByAnswer_IdOrderByCreatedAtAsc(answerId).stream()
        .map(
            comment ->
                CommentResponse.of(
                    comment,
                    MemberSummaryResponse.of(
                        comment.getUserId(), String.valueOf(comment.getUserId()), 0)))
        .toList();
  }

  @Transactional
  public CommentResponse createComment(Long answerId, CommentCreateRequest request, Long userId) {
    Answer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

    Comment comment = Comment.createForAnswer(userId, answer, request.content());
    answerRepository.incrementCommentCount(answerId);
    Comment saved = commentRepository.save(comment);
    MemberSummaryResponse author =
        MemberSummaryResponse.of(saved.getUserId(), String.valueOf(saved.getUserId()), 0);
    eventPublisher.publishEvent(
        new QnaCommentCreated(userId, answer.getQuestion().getId(), answerId, saved.getId()));
    return CommentResponse.of(saved, author);
  }

  @Transactional
  public CommentResponse updateComment(Long commentId, CommentUpdateRequest request, Long userId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

    if (!comment.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    comment.update(request.content());
    MemberSummaryResponse author =
        MemberSummaryResponse.of(comment.getUserId(), String.valueOf(comment.getUserId()), 0);
    return CommentResponse.of(comment, author);
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

    if (!comment.isAuthor(userId)) {
      throw new ForbiddenQnaActionException();
    }

    Answer answer = comment.getAnswer();
    commentRepository.delete(comment);
    answerRepository.decrementCommentCount(answer.getId());
    eventPublisher.publishEvent(
        new QnaCommentDeleted(userId, answer.getQuestion().getId(), answer.getId(), commentId));
  }
}

package com.study.qna.application;

import com.study.qna.application.dto.request.comment.CommentCreateRequest;
import com.study.qna.application.dto.response.comment.CommentResponse;
import com.study.qna.application.dto.response.common.MemberSummaryResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Comment;
import com.study.qna.domain.exception.AnswerNotFoundException;
import com.study.qna.infrastructure.AnswerRepository;
import com.study.qna.infrastructure.CommentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service("qnaCommentService")
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final AnswerRepository answerRepository;

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
    answer.incrementCommentCount();
    Comment saved = commentRepository.save(comment);
    MemberSummaryResponse author =
        MemberSummaryResponse.of(saved.getUserId(), String.valueOf(saved.getUserId()), 0);
    return CommentResponse.of(saved, author);
  }
}
